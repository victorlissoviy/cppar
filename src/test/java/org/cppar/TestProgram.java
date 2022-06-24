package org.cppar;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestProgram {
  private final String testLine = "test-line";
  private final Path p = Paths.get("src/test/resources");

  public TestProgram() {
    if (!Files.exists(p)) {
      try {
        Files.createDirectory(p);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Test
  public void testMain_1() {
    String[] mas = {};
    boolean as = false;
    try {
      Program.main(mas);
    } catch (RuntimeException e) {
      as = true;
    } finally {
      assert as : "Повинен бути вихід з програми, якщо програма запущена без аргументів";
    }
  }

  private void prepareFiles(Path from, Path to, Path fileNameFrom) {
    try {
      if (!Files.exists(from)) {
        Files.createDirectory(from);
      }
      if (!Files.exists(to)) {
        Files.createDirectory(to);
      }
      PrintWriter pw = new PrintWriter(fileNameFrom.toString());
      BufferedWriter bw = new BufferedWriter(pw);
      bw.write(testLine);
      bw.close();
      pw.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void removeFiles(Path fileNameFrom, Path fileNameTo, Path from, Path to) {
    try {
      if (Files.exists(fileNameFrom)) {
        Files.deleteIfExists(fileNameFrom);
      }
      if (Files.exists(from)) {
        Files.deleteIfExists(from);
      }
      if (Files.exists(fileNameTo)) {
        Files.deleteIfExists(fileNameTo);
      }
      if (Files.exists(to)) {
        Files.deleteIfExists(to);
      }
    } catch (IOException ignored) {
    }
  }

  @Test
  public void testMain_2() {
    Path from = Paths.get(p + "/from2/");
    Path to = Paths.get(p + "/to2/");
    Path fileNameFrom = Paths.get(from + "/test.txt");
    Path fileNameTo = Paths.get(to + "/test.txt");
    removeFiles(fileNameFrom, fileNameTo, from, to);
    prepareFiles(from, to, fileNameFrom);
    String[] mas = {from + "/", to + "/"};
    Program.main(mas);
    try (BufferedReader br = new BufferedReader(new FileReader(fileNameTo.toString()))) {
      String line = br.readLine();
      assert line.equals(testLine) : "Копіювання без параметрів не вдале";
      assert br.readLine() == null : "Копіювання без параметрів не вдале";
    } catch (IOException ignored) {

    }
    removeFiles(fileNameFrom, fileNameTo, from, to);
  }

  @Test
  public void testMain_3() {
    Path from = Paths.get(p + "/from3/");
    Path to = Paths.get(p + "/to3/");
    Path fileNameFrom = Paths.get(from + "/test.txt");
    Path fileNameTo = Paths.get(to + "/test.txt");
    removeFiles(fileNameFrom, fileNameTo, from, to);
    prepareFiles(from, to, fileNameFrom);
    String[] mas = {"-m", from + "/", to + "/"};
    Program.main(mas);
    try (BufferedReader br = new BufferedReader(new FileReader(fileNameTo.toString()))) {
      String line = br.readLine();
      assert line.equals(testLine) : "Копіювання без параметрів не вдале";
      assert br.readLine() == null : "Копіювання без параметрів не вдале";
      assert !Files.exists(fileNameFrom) : "Переміщення не вдале, вихідний файл залишився";
    } catch (IOException ignored) {
    }
    removeFiles(fileNameFrom, fileNameTo, from, to);
  }
}