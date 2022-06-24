package org.cppar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.CopyOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Objects;

public class Program {
  /**
   * list directories from path tree copy.
   */
  private final List<Path> listDirectories = new ArrayList<>();
  /**
   * map show source and destination copy files.
   */
  private final Map<Path, Path> map = new LinkedHashMap<>();
  /**
   * Iterator to iter on map.
   */
  private final Iterator<Map.Entry<Path, Path>> iterator;
  /**
   * count of active threads.
   */
  private int activeThreads = 0;
  /**
   * count of all possible active threads.
   */
  private final int countThreads;
  /**
   * move or copy files.
   */
  private final boolean move;
  /**
   * Copy options on work.
   */
  private CopyOption co;

  /**
   * @param moveFile    or copy files
   * @param update      if exist file rewrite them
   * @param force       do not answer just do it
   * @param count       count possible threads
   * @param sources     directory
   * @param destination directory
   */
  public Program(final boolean moveFile,
                 final boolean update,
                 final boolean force,
                 final int count,
                 final List<Path> sources,
                 final Path destination) {
    this.move = moveFile;
    this.countThreads = count;
    co = StandardCopyOption.COPY_ATTRIBUTES;
    if (update || force || move) {
      co = StandardCopyOption.REPLACE_EXISTING;
    }
    for (Path p : sources) {
      this.addFilesAndCreateFolders(p, destination);
    }
    iterator = map.entrySet().iterator();
  }

  /**
   * function to scan all folder files.
   *
   * @param source      directory or file
   * @param destination directory
   */
  private void addFilesAndCreateFolders(final Path source,
                                        final Path destination) {
    String name = source.getFileName().toString();
    String nameDest = destination + "/" + name;
    Path pathDest = Paths.get(nameDest);
    if (Files.isDirectory(source)) {
      listDirectories.add(source);
      if (new File(nameDest).mkdir()) {
        System.out.println("Створення теки:" + nameDest);
      }
      File[] files = new File(source.toString()).listFiles();
      if (files != null) {
        for (File file : files) {
          addFilesAndCreateFolders(file.toPath(), pathDest);
        }
      }
    } else {
      map.put(source, destination);
    }
  }

  /**
   * Main function on program.
   */
  public void work() {
    Thread[] threads = new Thread[this.countThreads];
    activeThreads = countThreads;
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(this::copy);
      threads[i].start();
    }
  }

  private void copy() {
    Path source;
    Path destination;
    while (true) {
      synchronized (this) {
        if (!map.isEmpty()) {
          Map.Entry<Path, Path> entry = iterator.next();
          iterator.remove();
          source = entry.getKey();
          destination = Paths.get(
                  entry.getValue() + "/"
                          + source.getFileName());
        } else {
          activeThreads -= 1;
          if (activeThreads == 0) {
            if (move) {
              while (listDirectories.size() != 0) {
                Path path = listDirectories.remove(listDirectories.size() - 1);
                //Перевірка чи тека є пустою і спроба її видалити
                try {
                  if (Objects.requireNonNull(
                          new File(path.toString())
                                  .listFiles())
                          .length == 0
                          && Files.deleteIfExists(path)) {
                    System.out.println("Видалення теки: " + path);
                  }
                } catch (IOException e) {
                  System.out.println("Помилка видалення теки: "
                          + path + ". " + e);
                }
              }
            }
          }
          break;
        }
      }
      System.out.println(source + " -> " + destination);
      try {
        if (move) {
          Files.move(source, destination, co);
        } else {
          Files.copy(source, destination, co);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * @param args from arguments commandline
   */
  public static void main(final String[] args) {
    if (args.length < 2) {
      System.out.println("Не нададо аргументів, вихід");
      throw new RuntimeException("Не нададо аргументів, вихід");
    }
    boolean move = false;
    boolean update = false;
    boolean force = false;
    int count = Runtime.getRuntime().availableProcessors() * 2;
    List<Path> listSources = new ArrayList<>();
    for (int i = 0; i < args.length - 1; i++) {
      switch (args[i]) {
        case "-m":
          move = true;
          break;
        case "-u":
          update = true;
          break;
        case "-c":
          i += 1;
          count = Integer.parseInt(args[i]);
          break;
        case "-f":
          force = true;
          break;
        default:
          break;
      }
      String src = args[i];
      File test = new File(src);
      if (!(test.isFile() || test.isDirectory())) {
        continue;
      }
      if (src.endsWith("/")) {
        File[] files = test.listFiles();
        if (files != null) {
          for (File file : files) {
            listSources.add(Paths.get(file.getAbsolutePath()));
          }
        }
      } else {
        listSources.add(Paths.get(src));
      }
    }
    if (listSources.size() == 0) {
      System.out.println("Джерела для копіювання не знайдено, вихід");
      System.exit(0);
    }
    Path destination = Paths.get(args[args.length - 1]);
    System.out.println("Кількість одночасних процесів: " + count);
    Program p = new Program(move,
            update,
            force,
            count,
            listSources,
            destination);
    p.work();
  }
}
