package com.danius.fireeditor.savefile.wireless;

import com.danius.fireeditor.savefile.Constants;
import com.danius.fireeditor.util.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Du26Block {
    public boolean isWest;
    private int teamSize;
    private byte[] header;
    public List<DuTeam> teamList;
    private List<byte[]> unknownList;
    public DuTeam playerTeam;
    private byte[] rawSpotPass;
    private byte[] rawExtra;

    public Du26Block(byte[] bytes, boolean isWest) {
        this.isWest = isWest;
        this.teamSize = teamSize(isWest);
        this.header = new byte[0x6];
        this.teamList = new ArrayList<>();
        this.unknownList = new ArrayList<>();
        this.playerTeam = null;
        this.rawSpotPass = new byte[0];
        this.rawExtra = new byte[0];
        if (bytes == null || bytes.length == 0) return;
        int offset = 0;
        //Block Header
        int headerSize = Math.min(0x6, bytes.length);
        this.header = Arrays.copyOfRange(bytes, 0x0, headerSize);
        offset += header.length;
        offset = readEncounters(bytes, offset);
        offset += 1; //Wireless Team Count
        offset += 1; //Other Unknown Count Byte
        //Unknown
        if (offset >= bytes.length) return;
        int unknownCount = bytes[offset];
        offset++; //Unknown Count
        for (int i = 0; i < unknownCount; i++) {
            if (offset >= bytes.length) break;
            int unknownSize = Math.min(0x29, bytes.length - offset);
            byte[] what = Arrays.copyOfRange(bytes, offset, offset + unknownSize);
            offset += unknownSize; // Use actual copied size
            unknownList.add(what);
        }
        //StreetPass Team
        System.out.println("\nSTREETPASS TEAM:");
        if (offset >= bytes.length) return;
        int streetPassSize = Math.min(teamSize + DuTeam.HEADER_SIZE, bytes.length - offset);
        playerTeam = new DuTeam
                (Arrays.copyOfRange(bytes, offset, offset + streetPassSize));
        offset += streetPassSize; // Use actual copied size, not playerTeam.length()
        //SpotPass
        if (offset >= bytes.length) return;
        int spotPassSize = Math.min(0x1C1, bytes.length - offset);
        this.rawSpotPass = Arrays.copyOfRange(bytes, offset, offset + spotPassSize);
        offset += spotPassSize; // Use actual copied size
        //Extra Data
        if (offset >= bytes.length) return;
        this.rawExtra = Arrays.copyOfRange(bytes, offset, bytes.length);
    }

    /*
    Double Duel
    0x5 Bitflags Double Duel Unlock (useless, set after first match, nothing happens if unset)
    0xD Bitflags Double Duel Beaten
    0x15 Double Duel Scores (1 byte)
     */
    public boolean isDuelBeaten(int bit) {
        int point = 0xD;
        return Hex.hasBitFlag(rawExtra, point, bit);
    }

    public void setDuelBeaten(int bit, boolean set) {
        int point = 0xD;
        Hex.setBitFlag(rawExtra, point, bit, set);
    }

    public int getDuelScore(int slot) {
        int point = 0x15;
        return rawExtra[point + slot + 1] & 0xFF;
    }

    public void setDuelScore(int slot, int value) {
        int point = 0x15;
        rawExtra[point + slot] = (byte) (value & 0xFF);
    }

    public int dlcTurn(int slot) {
        int point = 0x56;
        return rawExtra[point + slot + 1] & 0xFF;
    }

    public void setDlcTurn(int slot, int value) {
        int point = 0x56;
        rawExtra[point + slot + 1] = (byte) (value & 0xFF);
    }

    public static int teamSize(boolean isWest) {
        int settingSize = (isWest) ? DuTeam.US_NAME_TEAM : DuTeam.JP_NAME_TEAM;
        settingSize += 0x47;
        int messageSize = (isWest) ? DuTeam.US_MESSAGE : DuTeam.JP_MESSAGE;
        messageSize *= 4;
        //Adds the unit size
        int weaponSize = (isWest) ? DuTeam.US_WEAPON : DuTeam.JP_WEAPON;
        int unitName = (isWest) ? DuTeam.US_NAME_LOG : DuTeam.JP_NAME_LOG;
        int rawBlock1 = 0x21;
        int weaponTotal = (weaponSize + 0x4) * 5;
        int rawBlock2 = 0x18;
        int rawEnd = unitName + 0x1E;
        //Unit Size US: 0x12F (303) | JP: 0xD3 (211)
        int unitSize = rawBlock1 + weaponTotal + rawBlock2 + rawEnd;
        return (unitSize * 10) + settingSize + messageSize;
    }

    public int readEncounters(byte[] bytes, int offset) {
        teamList = new ArrayList<>();
        if (offset >= bytes.length) return offset;
        int teamCount = bytes[offset] & 0xFF;
        if (teamCount == 0) return offset;
        offset++; //Team Count byte
        //Team data
        System.out.println("\\nWIRELESS TEAMS:");
        for (int i = 0; i < teamCount; i++) {
            if (offset >= bytes.length) break;
            int total = DuTeam.HEADER_SIZE + teamSize + 1;
            int actualSize = Math.min(total, bytes.length - offset);
            byte[] teamBytes = Arrays.copyOfRange(bytes, offset, offset + actualSize);
            DuTeam duTeam = new DuTeam(teamBytes);
            teamList.add(duTeam);
            offset += actualSize; // Use actual copied size
            System.out.println();
        }
        return offset;
    }


    public void addSpotPass() {
        String path = Constants.RES_BLOCK + "rawSpotpassShort";
        byte[] blockSpot;
        try {
            java.io.InputStream is = UnitDu.class.getResourceAsStream(path);
            if (is != null) {
                blockSpot = is.readAllBytes();
            } else {
                // Create default block if template file is missing
                blockSpot = new byte[0x12F]; // Default size for SpotPass
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] header = Hex.toByte("AD 55 0A 19 01");
        int offset = Hex.indexOf(rawSpotPass, header, 0x0, 3);
        if (offset <= 0 || offset >= rawSpotPass.length || offset + blockSpot.length > rawSpotPass.length) return;
        System.arraycopy(blockSpot, 0, rawSpotPass, offset, blockSpot.length);
    }

    public byte[] bytes() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            //Header
            outputStream.write(header); //Block header
            //Wireless Teams
            outputStream.write(teamList.size()); //Wireless Team Count
            for (DuTeam team : teamList) outputStream.write(team.bytes());
            //Unknown
            outputStream.write(0); // ? Does not seem to be a byte count
            outputStream.write(unknownList.size()); //Unknown Count
            for (byte[] unknown : unknownList) outputStream.write(unknown);
            //Player's Team
            if (playerTeam != null) {
                outputStream.write(playerTeam.bytes());
            } else {
                // Write empty team data for null playerTeam
                outputStream.write(new byte[teamSize + DuTeam.HEADER_SIZE]);
            }
            //SpotPass
            outputStream.write(rawSpotPass);
            //Extra
            outputStream.write(rawExtra);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return outputStream.toByteArray();
    }

    public void changeRegion(boolean isWest) {
        //Wireless Teams
        for (DuTeam team : teamList) team.changeRegion(isWest);
        //Own Team
        if (playerTeam != null) {
            playerTeam.changeRegion(isWest);
        }
        this.isWest = isWest;
    }

    public int length() {
        byte[] bytes = bytes();
        if (bytes != null) {
            return bytes.length;
        }
        return 0;
    }
}


