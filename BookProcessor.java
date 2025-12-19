import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.*;

/*
Names: Partner A Name, Partner B Name
Date: 2025-12-18
Purpose: Single-thread vs multi-thread processing of large Project Gutenberg texts.
*/

public class BookProcessor {

// Pick two big books (plain text)
// War and Peace: https://www.gutenberg.org/files/2600/2600-0.txt [oai_citation:0‡Project Gutenberg](https://www.gutenberg.org/files/2600/2600-0.txt?utm_source=chatgpt.com)
// Moby-Dick: https://www.gutenberg.org/files/2701/2701-0.txt [oai_citation:1‡Project Gutenberg](https://www.gutenberg.org/files/2701/2701-0.txt?utm_source=chatgpt.com)
private static final String BOOK1_URL = "https://www.gutenberg.org/files/2600/2600-0.txt";
private static final String BOOK2_URL = "https://www.gutenberg.org/files/2701/2701-0.txt";

private static final Path DATA_DIR = Paths.get("data");
private static final Path OUT_DIR = Paths.get("output");

private static final Path BOOK1_PATH = DATA_DIR.resolve("war_and_peace.txt");
private static final Path BOOK2_PATH = DATA_DIR.resolve("moby_dick.txt");

private static final Path BOOK1_OUT = OUT_DIR.resolve("war_and_peace_OUT.txt");
private static final Path BOOK2_OUT = OUT_DIR.resolve("moby_dick_OUT.txt");

public static void main(String[] args) throws Exception {
Files.createDirectories(DATA_DIR);
Files.createDirectories(OUT_DIR);

ensureDownloaded(BOOK1_URL, BOOK1_PATH);
ensureDownloaded(BOOK2_URL, BOOK2_PATH);

// PARTNER A: Single-threaded processing + timing
// We intentionally use a slow transformation to demonstrate String immutability costs.
long start = System.nanoTime();
processSingleThread(BOOK1_PATH, BOOK1_OUT);
processSingleThread(BOOK2_PATH, BOOK2_OUT);
long end = System.nanoTime();

double seconds = (end - start) / 1_000_000_000.0;
System.out.printf("Single-thread total time: %.3f seconds%n", seconds);
}

private static void ensureDownloaded(String url, Path dest) throws IOException {
if (Files.exists(dest) && Files.size(dest) > 0) return;

System.out.println("Downloading: " + url);
try (InputStream in = new URL(url).openStream()) {
Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
}
System.out.println("Saved to: " + dest.toAbsolutePath());
}

private static void processSingleThread(Path inFile, Path outFile) throws IOException {
long start = System.nanoTime();

try (BufferedReader br = Files.newBufferedReader(inFile, StandardCharsets.UTF_8);
BufferedWriter bw = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {

String line;
while ((line = br.readLine()) != null) {
// Slow on purpose: build a NEW String repeatedly (Strings are immutable)
String transformed = slowUppercase(line);
bw.write(transformed);
bw.newLine();
}
}

long end = System.nanoTime();
System.out.printf("Processed %s in %.3f seconds%n",
inFile.getFileName(), (end - start) / 1_000_000_000.0);
}

// Very slow on purpose (shows immutability overhead)
private static String slowUppercase(String s) {
String out = "";
for (int i = 0; i < s.length(); i++) {
out = out + Character.toUpperCase(s.charAt(i));
}
return out;
}
}
