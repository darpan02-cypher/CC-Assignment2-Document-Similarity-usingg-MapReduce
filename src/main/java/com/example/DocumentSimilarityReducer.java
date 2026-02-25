package com.example;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;
import java.util.*;

public class DocumentSimilarityReducer extends Reducer<Text, Text, Text, Text> {

    private static class Document {
        String id;
        Set<String> words;

        Document(String id, Set<String> words) {
            this.id = id;
            this.words = words;
        }
    }

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        List<Document> docs = new ArrayList<>();

        for (Text val : values) {
            String[] parts = val.toString().split(":");
            if (parts.length < 2)
                continue;

            String docId = parts[0];
            String[] wordList = parts[1].split(",");
            Set<String> words = new HashSet<>(Arrays.asList(wordList));
            docs.add(new Document(docId, words));
        }

        // Compare all pairs
        for (int i = 0; i < docs.size(); i++) {
            for (int j = i + 1; j < docs.size(); j++) {
                Document d1 = docs.get(i);
                Document d2 = docs.get(j);

                double similarity = calculateJaccard(d1.words, d2.words);

                String pair = "\"" + d1.id + ", " + d2.id + " Similarity: " + String.format("%.2f", similarity) + "\"";
                context.write(new Text(pair), new Text(""));
            }
        }
    }

    private double calculateJaccard(Set<String> s1, Set<String> s2) {
        if (s1.isEmpty() && s2.isEmpty())
            return 1.0;

        Set<String> intersection = new HashSet<>(s1);
        intersection.retainAll(s2);

        Set<String> union = new HashSet<>(s1);
        union.addAll(s2);

        return (double) intersection.size() / union.size();
    }
}
