package com.mcglynn.rvo.stream;

import java.util.List;

public class StreamMarkerData {

    private List<Index> indices;

    public StreamMarkerData(List<Index> indices) {
        this.indices = indices;
    }

    public List<Index> getIndices() {
        return indices;
    }

    public static final class Index {
        private int markerOffset;
        private int dataOffset;
        private int dataSize;

        public Index(int markerOffset, int dataOffset, int dataSize) {
            this.markerOffset = markerOffset;
            this.dataOffset = dataOffset;
            this.dataSize = dataSize;
        }

        public int getMarkerOffset() {
            return markerOffset;
        }

        public int getDataOffset() {
            return dataOffset;
        }

        public int getDataSize() {
            return dataSize;
        }
    }
}
