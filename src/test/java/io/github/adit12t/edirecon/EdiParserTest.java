package io.github.adit12t.edirecon;

import io.github.adit12t.edirecon.Model.MemberRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EdiParserTest {

    @Test
    void splitsAFileIntoMemberLoops() throws IOException {
        Path file = Files.createTempFile("sample", ".edi");
        Files.writeString(file, """
                ST*834*0001~
                BGN*00*TEST*20260105*0600****2~
                INS*Y*18*030*XN*A*E**FT~
                REF*0F*M1~
                NM1*IL*1*SAMPLE*ALEX~
                INS*Y*18*030*XN*A*E**FT~
                REF*0F*M2~
                NM1*IL*1*SAMPLE*BLAKE~
                SE*9*0001~
                """);
        try {
            List<MemberRecord> members = new EdiParser().parse(file);

            assertEquals(2, members.size());
            assertEquals("M1", members.get(0).memberId());
            assertEquals(List.of("INS*Y*18*030*XN*A*E**FT", "REF*0F*M1", "NM1*IL*1*SAMPLE*ALEX"),
                    members.get(0).segments());
            assertEquals("M2", members.get(1).memberId());
            assertEquals(3, members.get(1).segments().size());
        } finally {
            Files.delete(file);
        }
    }
}
