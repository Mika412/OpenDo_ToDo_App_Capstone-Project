package com.mykha.opendo.objects;

public class ListTask {
        String docId;
        int id;
        String name;
        boolean complete;

        public ListTask(String docId, int id, String name, boolean complete) {
            this.docId = docId;
            this.id = id;
            this.name = name;
            this.complete = complete;
        }

        public String getDocId(){
            return docId;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public boolean getComplete() {
            return complete;
        }

        public void setComplete(boolean name) {
        this.complete = complete;
    }


}
