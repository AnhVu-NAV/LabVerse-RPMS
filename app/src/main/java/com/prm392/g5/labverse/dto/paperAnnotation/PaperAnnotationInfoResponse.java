package com.prm392.g5.labverse.dto.paperAnnotation;

public class PaperAnnotationInfoResponse {
        private String id;
        private String annotationS3Key;
        private String updateAt;

        public String getId() {
                return id;
        }

        public String getAnnotationS3Key() {
                return annotationS3Key;
        }

        public String getUpdateAt() {
                return updateAt;
        }
}
