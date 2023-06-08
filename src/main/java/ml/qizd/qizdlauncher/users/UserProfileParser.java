package ml.qizd.qizdlauncher.users;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserProfileParser {
    static class ParsingException extends Exception { }

    private static NoAuthUserProfile parseNoAuth(FileReader reader) throws IOException, ParsingException {
        int nameLength = reader.read();
        char[] name = new char[nameLength];
        if (reader.read(name, 0, nameLength) == -1)
            throw new ParsingException();

        return new NoAuthUserProfile(new String(name));
    }

    private static ElyByUserProfile parseElyBy(FileReader reader) throws IOException, ParsingException {
        int nameLength = reader.read();
        char[] name = new char[nameLength];
        if (reader.read(name, 0, nameLength) == -1)
            throw new ParsingException();

        char[] accessToken = new char[ElyByUserProfile.ACCESS_TOKEN_LENGTH];
        if (reader.read(accessToken, 0, ElyByUserProfile.ACCESS_TOKEN_LENGTH) == -1)
            throw new ParsingException();

        char[] UUID = new char[ElyByUserProfile.UUID_LENGTH];
        if (reader.read(UUID, 0, ElyByUserProfile.UUID_LENGTH) == -1)
            throw new ParsingException();

        return new ElyByUserProfile(new String(name), new String(accessToken), new String(UUID));
    }

    public static List<UserProfile> parse(FileReader reader) throws IOException, ParsingException {
        List<UserProfile> list = new ArrayList<>();

        int c;
        while ((c = reader.read()) != -1) {
            switch (c) {
                case 0 -> list.add(parseNoAuth(reader));
                case 1 -> list.add(parseElyBy(reader));
                default -> throw new ParsingException();
            }
        }

        return list;
    }
}
