package com.example.fitnessapp.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeminiModels {

    public static class Request {
        private List<Content> contents;

        public Request(List<Content> contents) {
            this.contents = contents;
        }

        public List<Content> getContents() { return contents; }
    }

    public static class Content {
        private List<Part> parts;
        private String role;

        public Content(List<Part> parts) {
            this.parts = parts;
            this.role = "user";
        }

        public List<Part> getParts() { return parts; }
        public String getRole() { return role; }
    }

    public static class Part {
        private String text;

        public Part(String text) {
            this.text = text;
        }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    public static class Response {
        private List<Candidate> candidates;
        private Error error;

        public List<Candidate> getCandidates() { return candidates; }
        public Error getError() { return error; }
        public boolean hasError() { return error != null; }
    }

    public static class Candidate {
        private Content content;
        @SerializedName("finishReason")
        private String finishReason;

        public Content getContent() { return content; }
        public String getFinishReason() { return finishReason; }
    }

    public static class Error {
        private int code;
        private String message;
        private String status;

        public int getCode() { return code; }
        public String getMessage() { return message; }
        public String getStatus() { return status; }
    }
}
