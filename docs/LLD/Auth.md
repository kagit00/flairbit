
## Low-Level Design: OTP-Based Authentication System

### 1. Overview
This document provides a detailed low-level design (LLD) for the authentication module, including class structures, method signatures, internal algorithms, data models, and implementation details. It builds on the HLD, focusing on code-level specifics from the provided implementation. The design uses Spring Boot annotations, Nimbus JOSE for JWT, and Caffeine-like caching.

**Scope:** Covers controllers, services, filters, utilities, and supporting models. Assumes JPA entities (e.g., `User`, `Role`) and DTOs (e.g., `RequestOtpRequest`, `AuthResponse`) are defined elsewhere.

**Key Data Structures:**
- **OtpEntry:** Inner class for OTP storage (String otp, Instant generatedAt).
- **Caches:** `Cache<String, OtpEntry>` for OTPs (TTL implied); `Cache<String, AtomicInteger>` for rate limits.
- **Constants:** `OTP_REQUEST_INTERVAL = 30s`, `OTP_VALIDITY = 5min`.

### 2. Class Diagrams (Textual UML Representation)
#### Core Classes
```mermaid
AuthenticationController
├── @RestController, @RequestMapping("/auth")
├── Dependencies: AuthenticationService
├── Methods:
│   ├── requestOtp(RequestOtpRequest): ResponseEntity<String>
│   └── verifyOtp(VerifyOtpRequest): ResponseEntity<AuthResponse>

AuthenticationService (Interface)
├── Methods:
│   ├── void requestOtp(RequestOtpRequest)
│   └── AuthResponse verifyOtp(String email, String otp)

AuthenticationServiceImpl implements AuthenticationService
├── @Service, @Slf4j, @RequiredArgsConstructor
├── Dependencies: RoleRepository, Cache<OtpEntry>, Cache<AtomicInteger>, JavaMailSender, UserRepository, JwtUtils
├── Fields: OTP_REQUEST_INTERVAL, OTP_VALIDITY
├── Methods: (detailed below)
```

#### Security Components
```mermaid
AuthTokenFilter extends OncePerRequestFilter
├── @Component
├── Dependencies: JwtUtils, UserDetailsServiceImpl (@Autowired)
├── Methods:
│   ├── doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)
│   └── retrieveTokenAndUsername(HttpServletRequest): Map<String, String>

JwtAuthenticationEntryPoint implements AuthenticationEntryPoint
├── @Component
├── Methods:
│   └── commence(HttpServletRequest, HttpServletResponse, AuthenticationException)

JwtUtils
├── @Service
├── Dependencies: RSAKey (flairbitKeyPair)
├── Methods: (detailed below)
```

### 3. Detailed Method Breakdowns

#### AuthenticationServiceImpl
- **requestOtp(RequestOtpRequest request): void**
    - **Inputs:** `RequestOtpRequest` (email: String, notificationEnabled: boolean).
    - **Steps/Algorithm:**
        1. Normalize email: `trim().toLowerCase()`.
        2. Generate username: `BasicUtility.generateUsernameFromEmail(email)`.
        3. Fetch/create user:
            - Query `userRepository.findByEmail(email)`.
            - If absent: Build `User` (email, username, notificationEnabled); Add default role via `DefaultValuesPopulator.populateDefaultUserRoles(roleRepository)`; Save via `userRepository.save()`.
        4. Check notifications: If user exists and `!notificationEnabled`, throw `IllegalStateException`.
        5. Rate limit check:
            - Get/increment `AtomicInteger` from `rateLimitCache.get(email, k -> new AtomicInteger(0))`.
            - If >3, throw `ResponseStatusException(429, "Too many...")`.
        6. Cooldown check:
            - Get existing `OtpEntry` from `otpCache.getIfPresent(email)`.
            - If exists and `Duration.between(generatedAt, now) < 30s`, throw `ResponseStatusException(429, "OTP already...")`.
        7. Generate OTP: `generateOtp()` (not shown; assume random 6-digit string).
        8. Cache: `otpCache.put(email, new OtpEntry(otp, Instant.now()))`.
        9. Send email: `sendOtpEmail(email, otp)` (commented; uses `SimpleMailMessage` with subject/text).
        10. Log: `log.info("OTP sent to {} {}", email, otp)`.
    - **Exceptions:** Wrapped in try-catch; Re-throw with logging.
    - **Side Effects:** User creation, cache updates, email send.

- **verifyOtp(String email, String otp): AuthResponse**
    - **Inputs:** email (String), otp (String).
    - **Steps/Algorithm:**
        1. Retrieve: `OtpEntry entry = otpCache.getIfPresent(email)`.
        2. Validate OTP: If null or `!entry.getOtp().equals(otp)`, throw `BadCredentialsException("Invalid or expired OTP.")`.
        3. Validate expiration: If `Duration.between(entry.getGeneratedAt(), now) > 5min`, invalidate cache and throw `BadCredentialsException("OTP expired.")`.
        4. Fetch user: `userRepository.findByEmail(email).orElseThrow(UsernameNotFoundException)`.
        5. Invalidate: `otpCache.invalidate(email)`.
        6. Generate token: `jwtUtils.generateToken(user)` (via UserDetails? Code uses raw User; assume conversion).
        7. Build DTO: `UserDTO(id, email, username)`.
        8. Return: `new AuthResponse(token, userDTO)`.
    - **Exceptions:** As thrown.
    - **Side Effects:** Cache invalidation.

- **sendOtpEmail(String email, String otp): void** (Private)
    - Builds `SimpleMailMessage`: To=email, Subject="Your Login OTP", Text=OTP + expiration note.
    - Calls `mailSender.send(message)`.
    - No exceptions handled explicitly.

- **generateOtp(): String** (Private, not shown)
    - Assume: `String.format("%06d", new Random().nextInt(1000000))` for 6-digit OTP.

#### JwtUtils
- **generateToken(UserDetails userDetails): String**
    - **Inputs:** `UserDetails` (with username, authorities).
    - **Steps:**
        1. Build claims: `Map<String, Object>` with "roles" as Set<String> from authorities.
        2. Call `doGenerateToken(claims, userDetails.getUsername())`.
    - **Output:** Serialized SignedJWT.

- **doGenerateToken(Map<String, Object> claims, String subject): String** (Private)
    - **Algorithm:**
        1. Timestamps: now, issueTime=now, expiration=now + 1hr (3600000ms).
        2. Build `JWTClaimsSet`: subject, issuer="FlairBit", issue/exp times, claim("roles", roles).
        3. Build `JWSHeader`: RS256 algo, keyID from RSAKey.
        4. Create `SignedJWT(header, claimsSet)`.
        5. Sign: `signedJWT.sign(new RSASSASigner(flairbitKeyPair.toPrivateKey()))`.
        6. Serialize and return.
    - **Exceptions:** Wrap in RuntimeException.

- **getUsernameFromToken(String token): String**
    - Parse `SignedJWT`, return `getJWTClaimsSet().getSubject()`.
    - Catch exceptions: Return null.

- **validateToken(String token, UserDetails userDetails): boolean**
    - **Steps:**
        1. Parse `SignedJWT`.
        2. Verify signature: `jwt.verify(new RSASSAVerifier(flairbitKeyPair.toRSAPublicKey()))`.
        3. Extract: username=subject, expiration=expTime.
        4. Return: `username.equals(userDetails.getUsername()) && expiration.after(new Date())`.
    - **Exceptions:** Return false.

#### AuthTokenFilter
- **doFilterInternal(...): void**
    - **Steps:**
        1. Extract creds: `retrieveTokenAndUsername(request)` → Map with "username", "jwtToken".
        2. If username present and no current auth:
            - Load `UserDetails` via `userDetailsService.loadUserByUsername(username)`.
            - Validate: `jwtUtils.validateToken(jwtToken, userDetails)`.
            - If valid and has authorities: Build `UsernamePasswordAuthenticationToken` (userDetails, null, authorities); Set details; Set in `SecurityContextHolder`.
            - Else: Throw `AccessDeniedException`.
        3. Proceed: `filterChain.doFilter(request, response)`.
    - **Exception Handling:**
        - AccessDenied: 403, JSON error via `ErrorUtility.printError`.
        - JWT Errors (IllegalArg, ExpiredJwt, MalformedJwt): 401, JSON error.
        - General: 500, JSON error.

- **retrieveTokenAndUsername(HttpServletRequest request): Map<String, String>**
    - Extract header: "Authorization".
    - If starts with "Bearer ", substring(7) → jwtToken; Extract username via `jwtUtils.getUsernameFromToken(jwtToken)`.
    - Return HashMap with keys.

#### JwtAuthenticationEntryPoint
- **commence(...): void**
    - Calls `ErrorUtility.printError(e.getMessage(), response)` (sets 401 JSON).

### 4. Supporting Models (Assumed/Inferred)
- **RequestOtpRequest:** `@RequestBody` with `String email`, `boolean notificationEnabled`; `@Valid`.
- **VerifyOtpRequest:** `String email`, `String otp`; `@Valid`.
- **AuthResponse:** `String token`, `UserDTO user`.
- **UserDTO:** `Long id`, `String email`, `String username`.
- **User (Entity):** `@Entity` with id, email, username, roles (Set<Role>), boolean notificationEnabled.
- **OtpEntry:** Record/class with `String otp`, `Instant generatedAt`; Methods: getOtp(), getGeneratedAt().

### 5. Algorithms and Edge Cases
- **OTP Generation:** Random, 6-digit (secure? Recommend cryptographically secure random).
- **Rate Limiting:** In-memory per instance; For distributed, use Redis with TTL.
- **User Creation:** Idempotent (findOrCreate); Default role population via utility.
- **Edge Cases:**
    - Duplicate emails: Normalized to lowercase.
    - Cache Miss: Treat as invalid/expired.
    - Email Disabled: Block if user opt-out.
    - JWT Claims: Roles as Set<String> for Spring Security integration.
- **Thread Safety:** AtomicInteger for counters; Cache assumed concurrent.

### 6. Implementation Notes
- **Dependencies:** Spring Boot 3.x, Nimbus JOSE + JWT, JavaMail, Caffeine (for Cache).
- **Configs:** RSAKey from properties/beans; Cache TTLs externalized.
- **Testing:**
    - Unit: Mock repositories/caches; Test OTP flow.
    - Integration: @SpringBootTest with H2 DB, WireMock for mail.
- **Improvements:** Hash OTP in cache; Add OTP retry limits; Distributed locking for user creation.

---