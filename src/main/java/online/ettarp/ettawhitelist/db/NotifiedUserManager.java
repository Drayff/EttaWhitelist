package online.ettarp.ettawhitelist.db;

import com.google.gson.Gson;
import online.ettarp.ettawhitelist.EttaWhitelist;
import online.ettarp.ettawhitelist.models.NotifiedUser;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NotifiedUserManager {

    private EttaWhitelist plugin;
    Set<NotifiedUser> notifiedUsers = new HashSet<>();

    public NotifiedUserManager(EttaWhitelist plugin) {
        this.plugin = plugin;
    }

    public NotifiedUser getNotifiedUser(String uuid) {
        return notifiedUsers.stream().filter(notifiedUser -> notifiedUser.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public Set<NotifiedUser> getNotifiedUsers() {
        return notifiedUsers;
    }

    public void setNotifiedUsers(Set<NotifiedUser> notifiedUsers) {
        this.notifiedUsers = notifiedUsers;
    }

    public void saveNotifiedUsers() throws IOException {
        Gson gson = new Gson();
        File file = new File(plugin.getDataFolder().getAbsolutePath() + "/notifiedusers.json");
        file.getParentFile().mkdir();
        file.createNewFile();
        Writer writer = new FileWriter(file, false);
        gson.toJson(notifiedUsers, writer);
        writer.flush();
        writer.close();
    }

    public void loadNotifiedUsers() throws IOException {
        Gson gson = new Gson();
        File file = new File(plugin.getDataFolder().getAbsolutePath() + "/notifiedusers.json");
        if (file.exists()) {

            Reader reader = new FileReader(file);
            NotifiedUser[] n = gson.fromJson(reader, NotifiedUser[].class);
            getNotifiedUsers().addAll(Arrays.asList(n));

        }

    }
}
