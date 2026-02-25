package com.example;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class DocumentSimilarityMapper extends Mapper<LongWritable, Text, Text, Text> {
    private Text outputKey = new Text("SIMILARITY_KEY");
    private Text outputValue = new Text();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString().trim();
        if (line.isEmpty())
            return;

        // Assuming format: DocID Word1 Word2 ...
        String[] parts = line.split("\\s+");
        if (parts.length < 2)
            return;

        String docId = parts[0];
        Set<String> words = new HashSet<>();
        for (int i = 1; i < parts.length; i++) {
            words.add(parts[i].toLowerCase().replaceAll("[^a-z0-9]", ""));
        }

        if (!words.isEmpty()) {
            // Emit format: docId:word1,word2,word3
            StringBuilder sb = new StringBuilder(docId).append(":");
            boolean first = true;
            for (String word : words) {
                if (!word.isEmpty()) {
                    if (!first)
                        sb.append(",");
                    sb.append(word);
                    first = false;
                }
            }
            outputValue.set(sb.toString());
            context.write(outputKey, outputValue);
        }
    }
}
