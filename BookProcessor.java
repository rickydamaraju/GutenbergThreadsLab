import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.*;

/*
Names: Partner A Name, Partner B Name
Date: 2025-12-18

What we did:
- Downloaded two large Project Gutenberg texts.
- Single-thread: processed book1 then book2 sequentially.
- Multi-thread: processed both books at the same time using ExecutorService (2 threads).
- To make processing take noticeable time, we used slow string concatenation while uppercasing,
demonstrating String immutability overhead.
*/

public class BookProcessor {

// Two big books (plain text UTF-8)
private static final String BOOK1_URL = "https://www.gutenberg.org/files/2600/2600-0.txt"; // War and Peace
private static final String BOOK2_URL = "https://www.gutenberg.org/files/2701/2701-0.txt"; // Moby-Dick

private static final Path DATA_DIR = Paths.get("data");
private static final Path OUT_DIR = Paths.get("output");

private static final Path BOOK1_PATH = DATA_DIR.resolve("book1.txt");
private static final Path BOOK2_PATH = DATA_DIR.resolve("book2.txt");

private static final Path BOOK1_OUT_SINGLE = OUT_DIR.resolve("book1_SINGLE.txt");
private static final Path BOOK2_OUT_SINGLE = OUT_DIR.resolve("book2_SINGLE.txt");

private static final Path BOOK1_OUT_MULTI = OUT_DIR.resolve("book1_MULTI.txt");
private static final Path BOOK2_OUT_MULTI = OUT_DIR.resolve("book2_MULTI.txt");

public static void main(String[] args) throws Exception {
Files.createDirectories(DATA_DIR);
Files.createDirectories(OUT_DIR);

downloadIfMissing(BOOK1_URL, BOOK1_PATH);
downloadIfMissing(BOOK2_URL, BOOK2_PATH);

// ---------- Single-thread baseline ----------
long sStart = System.nanoTime();
processFile(BOOK1_PATH, BOOK1_OUT_SINGLE);
processFile(BOOK2_PATH, BOOK2_OUT_SINGLE);
long sEnd = System.nanoTime();

System.out.printf("Single-thread total: %.3f seconds%n",
(sEnd - sStart) / 1_000_000_000.0);

// ---------- Multi-thread using ExecutorService ----------
long mStart = System.nanoTime();
processBothFilesMultiThread();
long mEnd = System.nanoTime();

System.out.printf("Multi-thread total: %.3f seconds%n",
(mEnd - mStart) / 1_000_000_000.0);
}

private static void processBothFilesMultiThread() throws Exception {
ExecutorService pool = Executors.newFixedThreadPool(2);

Callable<Void> task1 = () -> {
processFile(BOOK1_PATH, BOOK1_OUT_MULTI);
return null;
};

Callable<Void> task2 = () -> {
processFile(BOOK2_PATH, BOOK2_OUT_MULTI);
return null;
};

Future<Void> f1 = pool.submit(task1);
Future<Void> f2 = pool.submit(task2);

// Wait for both to finish (propagates exceptions)
f1.get();
f2.get();

pool.shutdown();
}

private static void downloadIfMissing(String url, Path dest) throws IOException {
if (Files.exists(dest) && Files.size(dest) > 0) {
return;
}
System.out.println("Downloading: " + url);
try (InputStream in = new URL(url).openStream()) {
Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
}
System.out.println("Saved: " + dest.toAbsolutePath());
}

private static void processFile(Path inFile, Path outFile) throws IOException {
long start = System.nanoTime();

try (BufferedReader br = Files.newBufferedReader(inFile, StandardCharsets.UTF_8);
BufferedWriter bw = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {

String line;
while ((line = br.readLine()) != null) {
// Intentionally slow transformation
String transformed = slowUppercase(line);
bw.write(transformed);
bw.newLine();
}
}

long end = System.nanoTime();
System.out.printf("Processed %-12s -> %-16s in %.3f seconds%n",
inFile.getFileName(),
outFile.getFileName(),
(end - start) / 1_000_000_000.0);
}

// VERY slow on purpose: repeated String concatenation (Strings are immutable)
private static String slowUppercase(String s) {
String out = "";
for (int i = 0; i < s.length(); i++) {
out = out + Character.toUpperCase(s.charAt(i));
}
return out;
}
}