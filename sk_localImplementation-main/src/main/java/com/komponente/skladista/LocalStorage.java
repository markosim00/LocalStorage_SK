package com.komponente.skladista;

import com.fasterxml.jackson.databind.ObjectMapper;
import configuration.ConfigurationStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LocalStorage extends File implements Skladiste{

    private List<File> files = new ArrayList<File>();
    private User activeUser;
    private int numberOfFiles;
    private long tempSizeInBytes;

    public User getActiveUser() {
        return activeUser;
    }

    public void setActiveUser(User activeUser) {
        this.activeUser = activeUser;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public void addFileInStorage(File file){
        this.files.add(file);
    }

    public void removeFileFromStorage(File file){
        this.files.remove(file);
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles() {
        this.numberOfFiles = this.files.size();
    }

    public long getTempSizeInBytes() {
        return tempSizeInBytes;
    }

    public void setTempSizeInBytes(long tempSizeInBytes) {
        this.tempSizeInBytes = tempSizeInBytes;
    }

    public LocalStorage(String pathname, String username, String password) throws IOException{
        super(pathname);
        this.activeUser = new User(username, password, Korisnici.premium);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if (this.exists())
                throw new IOException("Vec postoji skladiste na zadatoj putanji");
            else if (!this.mkdirs())
                throw new IOException("Greska prilikom inicijalizacije skladista");
            else {
                objectMapper.writeValue(Paths.get(pathname + "/user.json").toFile(), this.activeUser);
                this.configureStorage();
                System.out.println("Skladiste je uspesno inicijalizovano");
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public LocalStorage(String pathname) {
        super(pathname);
    }


    @Override
    public boolean login(String username, String password) throws IOException{
        if(this.activeUser == null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                User user = objectMapper.readValue(Paths.get(this.getAbsolutePath() + "/user.json").toFile(), User.class);
                //for (User user : users) {
                    if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                        this.setActiveUser(user);
                        user.setActive(true);
                        return true;
                    }
                //}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void logout() {
        this.activeUser.setActive(false);
        this.setActiveUser(null);
    }

    @Override
    public boolean addUser(String username, String password, Korisnici tip) throws IOException {
        if(this.activeUser.getPrivilegije().equals(Korisnici.premium)) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<User> users = Arrays.asList(objectMapper.readValue(Paths.get(this.getAbsolutePath()+ "/user.json").toFile(), User[].class));
                for (User user : users) {
                    if (user.getUsername().equals(username))
                        return false;
                }
                User user = new User(username, password, tip);
                objectMapper.writeValue(Paths.get(this.getAbsolutePath()+ "/user.json").toFile(), user);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    @Override
    public boolean checkStorageConfig(File file) throws IOException {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            ConfigurationStorage configurationStorage = objectMapper.readValue(Paths.get(this.getAbsolutePath() + "/configuration.json").toFile(), ConfigurationStorage.class);
            if(this.getNumberOfFiles() == configurationStorage.getMaxNumberOfFiles())
                return false;
            if(this.getTempSizeInBytes() + file.length() > configurationStorage.getMaxSizeInBytes())
                return false;
            for(String ex : configurationStorage.getUnsupportedExtensions()){
                if(this.getFileExtension(file).equals(ex))
                    return false;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return true;
    }

    public void setMaxSizeInBytes(int maxSizeInBytes){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            ConfigurationStorage configurationStorage = new ConfigurationStorage.ConfigurationBuilder().maxSizeInBytes(maxSizeInBytes).build();
            objectMapper.writeValue(Paths.get(this.getAbsolutePath()+ "/configuration.json").toFile(), configurationStorage);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void setMaxNumberOfFiles(int maxNumberOfFiles){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            ConfigurationStorage configurationStorage = new ConfigurationStorage.ConfigurationBuilder().maxNumberOfFiles(numberOfFiles).build();
            objectMapper.writeValue(Paths.get(this.getAbsolutePath() + "/configuration.json").toFile(), configurationStorage);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void setUnsupportedExtensions(List<String> unsupportedExtensions){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            ConfigurationStorage configurationStorage = new ConfigurationStorage.ConfigurationBuilder().unsupportedExtensions(unsupportedExtensions).build();
            objectMapper.writeValue(Paths.get(this.getAbsolutePath() + "/configuration.json").toFile(), configurationStorage);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void addUnsupportedExtension(String extension){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            ConfigurationStorage configurationStorage = objectMapper.readValue(Paths.get("configuration.json").toFile(), ConfigurationStorage.class);
            configurationStorage.getUnsupportedExtensions().add(extension);
            objectMapper.writeValue(Paths.get(this.getAbsolutePath() + "/configuration.json").toFile(), configurationStorage);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void removeUnsupportedExtension(String extension){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            ConfigurationStorage configurationStorage = objectMapper.readValue(Paths.get("configuration.json").toFile(), ConfigurationStorage.class);
            configurationStorage.getUnsupportedExtensions().remove(extension);
            objectMapper.writeValue(Paths.get(this.getAbsolutePath() + "/configuration.json").toFile(), configurationStorage);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void configureStorage() {
        this.setMaxSizeInBytes(1024000);
        this.setMaxNumberOfFiles(100);
        this.setUnsupportedExtensions(new ArrayList<String>());
    }

    @Override
    public void createDirectory(String path) throws IOException{

        if(this.activeUser != null) {

            if(path.contains(this.getAbsolutePath())) {

                if (this.activeUser.isSave()) {
                    File file = new File(path);
                    if (this.checkStorageConfig(file)) {
                        if (!file.exists()) {
                            if (file.mkdir()) {
                                this.files.add(file);
                                this.setTempSizeInBytes(this.getTempSizeInBytes() + file.length());
                                this.setNumberOfFiles();
                                System.out.println("Direktorijum uspesno napravljen");
                            } else
                                System.out.println("Vec postoji direktorijum na zadatoj putanji");
                        } else
                            System.out.println("Greska prilikom kreiranja direktorijuma");
                    }
                } else
                    System.out.println("Kreiranje direktorijuma nije moguce");
            }
            else
                System.out.println("Prosledjena putanja se ne nalazi u skladistu");
        }
        else
            System.out.println("Morate se prvo prijaviti na skladiste");
    }

    @Override
    public void createFile(String path) throws IOException {
        if(this.activeUser != null) {
            if(path.contains(this.getAbsolutePath())) {
                if (this.activeUser.isSave()) {
                    File file = new File(path);
                    if (this.checkStorageConfig(file)) {
                        if (file.createNewFile()) {
                            this.files.add(file);
                            this.setTempSizeInBytes(this.getTempSizeInBytes() + file.length());
                            this.setNumberOfFiles();
                            System.out.println("Fajl uspesno kreiran");
                        } else
                            System.out.println("Vec postoji fajl na zadatoj putanji");
                    }
                } else
                    System.out.println("Kreiranje fajla nije moguce");
            }
            else
                System.out.println("Prosledjena putanja se ne nalazi u skladistu");
        }
        else
            System.out.println("Morate se prvo prijaviti na skladiste");
    }

    @Override
    public void moveFile(String path1, String path2) throws IOException{
        File file1 = new File(path1);
        File file2 = new File(path2);
        if(this.activeUser != null) {
            if(path1.contains(this.getAbsolutePath())) {
                if (this.activeUser.isSave()) {
                    Path temp = Files.move(file1.toPath(), file2.toPath());
                    if (temp != null)
                        System.out.println("Fajl uspesno premesten");
                    else
                        System.out.println("Greska prilikom premestanja fajla");
                } else
                    System.out.println("Korisnik nema privilegiju za premestanje fajlova");
            }
            else
                System.out.println("Prosledjena putanja se ne nalazi u skladistu");
        }
        else
            System.out.println("Morate se prvo prijaviti na skladiste");
    }

    @Override
    public void deleteFile(String path) throws IOException{
        File file = new File(path);
        if(this.activeUser != null) {
            if(path.contains(this.getAbsolutePath())) {
                if (this.activeUser.isDelete()) {
                    if (file.delete()) {
                        this.files.remove(file);
                        this.setNumberOfFiles();
                        this.setTempSizeInBytes(this.getTempSizeInBytes() - file.length());
                        System.out.println("Fajl uspesno obrisan");
                    } else
                        System.out.println("Neuspesno brisanje fajla");
                } else
                    System.out.println("Korisnik nema privilegiju za brisanje");
            }
            else
                System.out.println("Prosledjena putanja se ne nalazi u skladistu");
        }
        else
            System.out.println("Morate se prvo prijaviti na skladiste");
    }


    @Override
    public void deleteDirectory(String path) throws IOException {
        File directory = new File(path);
        if(this.activeUser != null) {
            if(path.contains(this.getAbsolutePath())) {
                if (this.activeUser.isDelete()) {
                    File[] allFiles = directory.listFiles();
                    if (allFiles != null) {
                        for (File file : allFiles)
                            deleteDirectory(file.getAbsolutePath());
                    }
                    if (!directory.delete())
                        System.out.println("Neuspesno brisanje direktorijuma");
                    else {
                        this.files.remove(directory);
                        this.setNumberOfFiles();
                        this.setTempSizeInBytes(this.getTempSizeInBytes() - directory.length());
                    }
                } else
                    System.out.println("Korisnik nema privilegiju za brisanje");
            }
            else
                System.out.println("Prosledjena putanja se ne nalazi u skladistu");
        }
        else
            System.out.println("Morate se prvo prijaviti na skladiste");
    }

    @Override
    public void downloadFile(String src, String dest) throws IOException{
        if(this.activeUser != null) {
            if(src.contains(this.getAbsolutePath())) {
                File file1 = new File(src);
                File file2 = new File(dest);
                if (this.activeUser.isDownload()) {
                    Path path = Files.copy(file1.toPath(), file2.toPath());
                    if (path != null)
                        System.out.println("Fajl je uspesno preuzet sa skladista");
                    else
                        System.out.println("Neuspesno preuzimanje");
                } else
                    System.out.println("Korisnik nema privilegiju za download");
            }
            else
                System.out.println("Prosledjena putanja izvora se ne nalazi u skladistu");
        }
        else
            System.out.println("Morate se prvo prijaviti na skladiste");
    }

    private String getFileExtension(File file){
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if(lastIndexOf == -1)
            return "";
        return name.substring(lastIndexOf);
    }

    @Override
    public String[] getAllFilesFromDirectory(String path) {
        File directory = new File(path);
        if(this.activeUser != null) {
            if(path.contains(this.getAbsolutePath())) {
                String[] allFiles = directory.list();
                return allFiles;
            }
            else{
                System.out.println("Prosledjena putanja se ne nalazi u skladistu");
                return null;
            }
        }
        else {
            System.out.println("Morate se prvo prijaviti na skladiste");
            return null;
        }
    }

    @Override
    public String[] getAllDirectoriesFromDirectory(String path) {
        File directory = new File(path);
        if(this.activeUser != null) {
            if(path.contains(this.getAbsolutePath())) {
                File[] allFiles = directory.listFiles();
                String[] directories = new String[1000];
                int idx = 0;
                if (allFiles != null) {
                    for (File file : allFiles) {
                        if (file.isDirectory())
                            directories[idx++] = file.getName();
                    }
                }
                return directories;
            }
            else{
                System.out.println("Prosledjena putanja se ne nalazi u skladistu");
                return null;
            }
        }
        else{
            System.out.println("Morate se prvo prijaviti na skladiste");
            return null;
        }
    }

    @Override
    public String[] getAllChildren(String directoryName, List<Object> allFiles) {
        if(this.activeUser != null) {
            if(directoryName.contains(this.getAbsolutePath())) {
                File directory = new File(directoryName);
                File[] fList = directory.listFiles();
                List<File> files = (List<File>) (Object) allFiles;
                if (fList != null) {
                    for (File file : fList) {
                        if (file.isFile())
                            files.add(file);
                        else if (file.isDirectory())
                            getAllChildren(file.getAbsolutePath(), (List<Object>) (File) files);
                    }
                }
                String[] fileNames = new String[1000];
                int idx = 0;
                for (File file : files) {
                    fileNames[idx++] = file.getName();
                }
                return fileNames;
            }
            else{
                System.out.println("Prosledjena putanja se ne nalazi na skladistu");
                return null;
            }
        }
        else{
            System.out.println("Morate se prvo prijaviti na skladiste");
            return null;
        }
    }

    @Override
    public String[] getFilesByExtension(String extension) {
        if(this.activeUser != null) {
            String[] filesWithExtension = new String[1000];
            int idx = 0;
            for (File file : this.files) {
                if (this.getFileExtension(file).equals(extension))
                    filesWithExtension[idx++] = file.getName();
            }
            return filesWithExtension;
        }
        else{
            System.out.println("Morate se prvo prijaviti na skladiste");
            return null;
        }
    }

    @Override
    public String[] getFilesSortedByNameAsc() {
        if(this.activeUser != null) {
            String[] fileNames = new String[1000];
            int idx = 0;
            for (File file : this.files) {
                if (file.isFile())
                    fileNames[idx++] = file.getName();
            }
            Arrays.sort(fileNames);
            return fileNames;
        }
        else{
            System.out.println("Morate se prvo prijaviti na skladiste");
            return null;
        }
    }

    @Override
    public String[] getFilesSortedByNameDesc() {
        String [] fileNames = this.getFilesSortedByNameAsc();
        Arrays.sort(fileNames, Collections.reverseOrder());
        return fileNames;
    }

    @Override
    public String[] getFilesSortedByDateAsc() {
        if(this.activeUser != null) {
            File[] allFiles = new File[1000];
            int idx = 0;
            for (File file : this.files) {
                allFiles[idx++] = file;
            }
            Arrays.sort(allFiles, Comparator.comparingLong(File::lastModified));
            String[] fileNames = new String[1000];
            int idx1 = 0;
            for (int i = 0; i < idx; i++) {
                fileNames[idx1++] = allFiles[i].getName();
            }
            return fileNames;
        }
        else{
            System.out.println("Morate se prvo prijaviti na skladiste");
            return null;
        }
    }

    @Override
    public String[] getFilesSortedByDateDesc() {
        if(this.activeUser != null) {
            File[] allFiles = new File[1000];
            int idx = 0;
            for (File file : this.files) {
                allFiles[idx++] = file;
            }
            Arrays.sort(allFiles, Comparator.comparingLong(File::lastModified).reversed());
            String[] fileNames = new String[1000];
            int idx1 = 0;
            for (int i = 0; i < idx; i++) {
                fileNames[idx1++] = allFiles[i].getName();
            }
            return fileNames;
        }
        else{
            System.out.println("Morate se prvo prijaviti na skladiste");
            return null;
        }
    }

    @Override
    public String[] getAllFilesInPeriod(long date1, long date2) {
        if(this.activeUser != null) {
            String[] fileNames = new String[1000];
            int idx = 0;
            for (File file : this.files) {
                if (file.lastModified() >= date1 && file.lastModified() <= date2)
                    fileNames[idx++] = file.getName();
            }
            return fileNames;
        }
        else{
            System.out.println("Morate se prvo prijaviti na skladiste");
            return null;
        }
    }

    @Override
    public String[] getAllFilesInPeriodInDirectory(long date1, long date2, String path) {
        File directory = new File(path);
        if(this.activeUser != null) {
            if(path.contains(this.getAbsolutePath())) {
                File[] allFiles = directory.listFiles();
                String[] fileNames = new String[1000];
                int idx = 0;
                for (int i = 0; i < allFiles.length; i++) {
                    if (allFiles[i].lastModified() >= date1 && allFiles[i].lastModified() <= date2)
                        fileNames[idx++] = allFiles[i].getName();
                }
                return fileNames;
            }
            else{
                System.out.println("Prosledjena putanja se ne nalazi na skladistu");
                return null;
            }
        }
        else{
            System.out.println("Morate se prvo prijaviti na skladiste");
            return null;
        }
    }
}
