# Assignment 2: Document Similarity using MapReduce

**Name:** Himanshi Shrivas

**Student ID:** 801454323

## Approach and Implementation

### Mapper Design
The `DocumentSimilarityMapper` reads lines from the input text file. Each line is assumed to start with a `DocID` followed by document text. 
- **Input**: `(LongWritable, Text)` - byte offset and line content.
- **Process**: It tokenizes the text into words, converts them to lowercase, and removes non-alphanumeric characters.
- **Output**: `(SIMILARITY_KEY, docId:word1,word2,...)`. It emits a constant key `SIMILARITY_KEY` so that all document word sets are sent to the same reducer for pair-wise comparison.

### Reducer Design
The `DocumentSimilarityReducer` receives all documents under the `SIMILARITY_KEY`.
- **Input**: `(SIMILARITY_KEY, Iterable<docId:word1,word2,...>)`.
- **Process**: It parses the values into a list of document objects (ID and word set). It then performs a nested loop to calculate the Jaccard Similarity for every unique pair of documents using the formula: `Intersection Size / Union Size`.
- **Output**: `("DocID1, DocID2 Similarity: X.XX", "")`.

### Overall Data Flow
1. **Input Stage**: Data is read from HDFS (e.g., `small_dataset.txt`).
2. **Map Stage**: Mapper tokenizes words and groups all docs under a single key.
3. **Shuffle/Sort**: Hadoop groups all document word sets together by the common key.
4. **Reduce Stage**: Reducer receives the full list of documents, computes Jaccard similarity for all pairs, and writes formatted results to HDFS.

---

## Setup and Execution

### ` Note: The below commands are the ones used for the Hands-on. You need to edit these commands appropriately towards your Assignment to avoid errors. `

### 1. **Start the Hadoop Cluster**

Run the following command to start the Hadoop cluster:

```bash
docker compose up -d
```

### 2. **Build the Code**

Build the code using Maven:

```bash
mvn clean package
```

### 4. **Copy JAR to Docker Container**

Copy the JAR file to the Hadoop ResourceManager container:

```bash
docker cp target/DocumentSimilarity-0.0.1-SNAPSHOT.jar resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 5. **Move Dataset to Docker Container**

Copy the dataset to the Hadoop ResourceManager container:

```bash
docker cp shared-folder/input/data/small_dataset.txt resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 6. **Connect to Docker Container**

Access the Hadoop ResourceManager container:

```bash
docker exec -it resourcemanager /bin/bash
```

Navigate to the Hadoop directory:

```bash
cd /opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 7. **Set Up HDFS**

Create a folder in HDFS for the input dataset:

```bash
hadoop fs -mkdir -p /input/data
```

Copy the input dataset to the HDFS folder:

```bash
hadoop fs -put ./small_dataset.txt /input/data
```

### 8. **Execute the MapReduce Job**

Run your MapReduce job using the following command: Here I got an error saying output already exists so I changed it to output1 instead as destination folder

```bash
hadoop jar /opt/hadoop-3.2.1/share/hadoop/mapreduce/DocumentSimilarity-0.0.1-SNAPSHOT.jar com.example.controller.DocumentSimilarityDriver /input/data/small_dataset.txt /output1
```

### 9. **View the Output**

To view the output of your MapReduce job, use:

```bash
hadoop fs -cat /output1/*
```

### 10. **Copy Output from HDFS to Local OS**

To copy the output from HDFS to your local machine:

1. Use the following command to copy from HDFS:
    ```bash
    hdfs dfs -get /output1 /opt/hadoop-3.2.1/share/hadoop/mapreduce/
    ```

2. use Docker to copy from the container to your local machine:
   ```bash
   exit 
   ```
    ```bash
    docker cp resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/output1/ shared-folder/output/
    ```
3. Commit and push to your repo so that we can able to see your output


---

## Challenges and Solutions

1. **Docker Container Conflicts**: Existing containers with same names were preventing cluster startup.
   - **Solution**: Performed a full cleanup using `docker rm -f` on conflicting IDs.
2. **Local Memory OOM (Exit 137)**: The full cluster with 9 containers was too heavy for local laptop RAM.
   - **Solution**: Scaled down to a single-node cluster (1 DataNode, 1 NodeManager) in `docker-compose.yml` and reduced memory settings in `hadoop.env`.
3. **Queue Mapping Error**: Job submission failed with "unknown queue: default".
   - **Solution**: Switched from `CapacityScheduler` to `FifoScheduler` for simpler local testing.
4. **HDFS Hostname Resolution**: DataNode upload failed with `UnresolvedAddressException`.
   - **Solution**: Configured `dfs.client.use.datanode.hostname=true` in `hadoop.env`.

---
## Sample Input

**Input from `small_dataset.txt`**
```
Document1 This is a sample document containing words
Document2 Another document that also has words
Document3 Sample text with different words
```
## Sample Output

**Output from `small_dataset.txt`**
```
"Document1, Document2 Similarity: 0.56"
"Document1, Document3 Similarity: 0.42"
"Document2, Document3 Similarity: 0.50"
```
## Obtained Output:
```text
"Document4, Document3 Similarity: 0.50" 
"Document4, Document2 Similarity: 0.18" 
"Document4, Document1 Similarity: 0.40" 
"Document3, Document2 Similarity: 0.10" 
"Document3, Document1 Similarity: 0.20" 
"Document2, Document1 Similarity: 0.18"
```
