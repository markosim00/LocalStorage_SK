package configuration;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationStorage {

    private long maxSizeInBytes;
    private int maxNumberOfFiles;
    private List<String> unsupportedExtensions = new ArrayList<String>();

    public ConfigurationStorage(ConfigurationBuilder builder){
        this.maxSizeInBytes = builder.maxSizeInBytes;
        this.maxNumberOfFiles = builder.maxNumberOfFiles;
        this.unsupportedExtensions = builder.unsupportedExtensions;
    }

    public long getMaxSizeInBytes() {
        return maxSizeInBytes;
    }

    public void setMaxSizeInBytes(long maxSizeInBytes) {
        this.maxSizeInBytes = maxSizeInBytes;
    }

    public int getMaxNumberOfFiles() {
        return maxNumberOfFiles;
    }

    public void setMaxNumberOfFiles(int maxNumberOfFiles) {
        this.maxNumberOfFiles = maxNumberOfFiles;
    }

    public List<String> getUnsupportedExtensions() {
        return unsupportedExtensions;
    }

    public void setUnsupportedExtensions(List<String> unsupportedExtensions) {
        this.unsupportedExtensions = unsupportedExtensions;
    }

    public static class ConfigurationBuilder{
        private long maxSizeInBytes;
        private int maxNumberOfFiles;
        private List<String> unsupportedExtensions = new ArrayList<String>();

        public ConfigurationBuilder maxSizeInBytes(long maxSizeInBytes){
            this.maxSizeInBytes = maxSizeInBytes;
            return this;
        }

        public ConfigurationBuilder maxNumberOfFiles(int maxNumberOfFiles){
            this.maxNumberOfFiles = maxNumberOfFiles;
            return this;
        }

        public ConfigurationBuilder unsupportedExtensions(List<String> unsupportedExtensions){
            this.unsupportedExtensions = unsupportedExtensions;
            return this;
        }

        public void addUnsupportedExtension(String extension){
            this.unsupportedExtensions.add(extension);
        }

        public void removeUnsupportedExtension(String extension){
            this.unsupportedExtensions.remove(extension);
        }

        public ConfigurationStorage build(){
            ConfigurationStorage configStorage = new ConfigurationStorage(this);
            return configStorage;
        }
    }

}
