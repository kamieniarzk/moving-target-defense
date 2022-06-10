package spzc.daemon.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CommandExecutor {

  public boolean executeCommand(String command) throws IOException, InterruptedException {
    var builder = new ProcessBuilder();
    var commandSeparated = command.split(" ");
    builder.inheritIO().command(commandSeparated);
    var process = builder.start();
    var streamConsumer = new StreamConsumer(process.getInputStream(), log::info);
    Executors.newSingleThreadExecutor().submit(streamConsumer);
    int exitCode = process.waitFor();
    return exitCode == 0;
  }

  @AllArgsConstructor
  private static class StreamConsumer implements Runnable {
    private InputStream inputStream;
    private Consumer<String> consumer;

    @Override
    public void run() {
      new BufferedReader(new InputStreamReader(inputStream)).lines()
          .forEach(consumer);
    }
  }
}
