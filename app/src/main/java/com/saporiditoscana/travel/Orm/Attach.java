package com.saporiditoscana.travel.Orm;

import com.google.gson.annotations.SerializedName;

import java.util.StringJoiner;

public class Attach{
    @SerializedName("FileName")
    private String FileName;
    @SerializedName("FileBase64")
    private String FileBase64;
    @SerializedName("MediaType")
    private String MediaType;

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        this.FileName = fileName;
    }

    public String getFileBase64() {
        return FileBase64;
    }

    public void setFileBase64(String fileBase64) {
        this.FileBase64 = fileBase64;
    }

    public String getMediaType() {
        return MediaType;
    }

    public void setMediaType(String mediaType) {
        MediaType = mediaType;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Attach.class.getSimpleName() + "[", "]")
                .add("FileName='" + FileName + "'")
                .add("FileBase64='" + FileBase64 + "'")
                .add("MediaType='" + MediaType + "'")
                .toString();
    }

    public String getFileType() {
        return getMediaType();
    }
}