package org.cppar;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NetConnect implements Closeable {
  /**
   * username to connect.
   */
  private final String username;
  /**
   * Path to key for connect.
   */
  private String keyPath;
  /**
   * SSH Client to connect remote server.
   */
  private SSHClient sshClient;
  /**
   * SFTP Client to manage files on remote server.
   */
  private SFTPClient sftpClient;
  /**
   * path to file or dir on remote server.
   */
  private Path path;
  /**
   * Host or ip remote server.
   */
  private final String host;

  /**
   * @param line full line to connect on server user@server:path_to_file.
   */
  public NetConnect(final String line) {
    if (!parseLine(line)) {
      throw new RuntimeException("Не вірний рядок для з'єднання: " + line);
    }
    String[] mas = line.split("@");
    username = mas[0];
    mas = mas[1].split(":");
    host = mas[0];
    if (mas.length == 2) {
      path = Paths.get(mas[1]);
    }
    System.out.println("Користувач: " + username);
    System.out.println("Хост: " + host);
    System.out.println("Директорія: " + path);
  }

  /**
   * @param line line to check if line is valid to connect
   *             user@server:path_to_file
   * @return check status
   */
  public static boolean parseLine(final String line) {
    if (!(line.contains("@") && line.contains(":") && line.contains("/"))) {
      return false;
    }
    String[] mas = line.split("@");
    for (String m : mas) {
      if (m.length() == 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * @param pathToKeyFile to key file.
   */
  public void setKeyPath(final String pathToKeyFile) {
    keyPath = pathToKeyFile;
  }

  /**
   * @throws IOException if connect is not success.
   */
  public void connect() throws IOException {
    sshClient = new SSHClient();
    sshClient.addHostKeyVerifier(new PromiscuousVerifier());
    sshClient.connect(host);
    sshClient.authPublickey(username, sshClient.loadKeys(keyPath));
    sftpClient = sshClient.newSFTPClient();
        /* TODO все це
        Відправка
         sftpClient.put("/storage/Картинки/Робочий стіл/Право/123.jpg", "./");
      sftpClient.get("123.jpg","./"); отримання файлу
      List<RemoteResourceInfo> ls = sftpClient.ls("."); отримаття файлів в теці
      for (RemoteResourceInfo l : ls) {
      Тип файлу REGULAR DIRECTORY
        System.out.println(sftpClient.type(l.getPath()));
        System.out.println(l.getPath()); місце файлу
      }
      sftpClient.mkdir("/home/admin/test-dir"); створення тек
         */
  }

  /**
   * Close connect when end work on remote server.
   * @throws IOException if close is not success
   */
  public void close() throws IOException {
    sftpClient.close();
    sshClient.close();
  }

  /**
   * @param from source file
   * @param to destination file
   * @throws IOException if copy is not success
   */
  public void copyFile(final String from, final String to) throws IOException {
    if (sshClient.isConnected()) {
      sftpClient.get(from, to);
    } else {
      throw new RuntimeException("Не створено підключення");
    }
  }

  //TODO функція для отримання дерева тек і файлів
}
