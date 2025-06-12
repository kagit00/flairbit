package com.dating.flairbit.processor;

import com.dating.flairbit.config.factory.CopyStreamSerializer;
import com.dating.flairbit.models.MatchSuggestion;
import com.dating.flairbit.utils.basic.BasicUtility;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class MatchSuggestionSerializer implements CopyStreamSerializer<MatchSuggestion> {
    private final List<MatchSuggestion> batch;
    private final String groupId;

    public MatchSuggestionSerializer(List<MatchSuggestion> batch, String groupId) {
        this.batch = batch;
        this.groupId = groupId;
    }

    @Override
    public void serialize(MatchSuggestion match, DataOutputStream out) throws IOException {
        out.writeShort(7);

        // UUID id
        out.writeInt(16);
        UUID id = match.getId() != null ? match.getId() : DefaultValuesPopulator.getUid2();
        out.writeLong(id.getMostSignificantBits());
        out.writeLong(id.getLeastSignificantBits());

        // groupId
        byte[] groupIdBytes = match.getGroupId().getBytes(StandardCharsets.UTF_8);
        out.writeInt(groupIdBytes.length);
        out.write(groupIdBytes);


        // referenceId
        byte[] refIdBytes = match.getParticipantId().getBytes(StandardCharsets.UTF_8);
        out.writeInt(refIdBytes.length);
        out.write(refIdBytes);

        // matchedReferenceId
        byte[] matchedRefIdBytes = match.getMatchedParticipantId().getBytes(StandardCharsets.UTF_8);
        out.writeInt(matchedRefIdBytes.length);
        out.write(matchedRefIdBytes);

        // compatibility score
        out.writeInt(8);
        out.writeDouble(match.getCompatibilityScore());

        //match suggestion type
        byte[] suggestionBytes = match.getMatchSuggestionType().getBytes(StandardCharsets.UTF_8);
        out.writeInt(suggestionBytes.length);
        out.write(suggestionBytes);

        BasicUtility.writeTimestamp(
                match.getCreatedAt() != null ? match.getCreatedAt() : DefaultValuesPopulator.getCurrentTimestamp(),
                out
        );
    }
}
