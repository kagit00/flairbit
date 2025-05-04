package com.dating.flairbit.repo;

import com.dating.flairbit.models.Lifestyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LifeStyleRepository extends JpaRepository<Lifestyle, UUID> {
}
