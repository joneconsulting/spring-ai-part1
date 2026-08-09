package com.example.rag.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** --demo 인자에 따라 해당 데모를 실행한다. */
@Component
public class DemoRunner implements ApplicationRunner {

    private final SimilarityDemo similarityDemo;
    private final ChunkingDemo chunkingDemo;
    private final MiniRagDemo miniRagDemo;

    public DemoRunner(SimilarityDemo similarityDemo,
                      ChunkingDemo chunkingDemo,
                      MiniRagDemo miniRagDemo) {
        this.similarityDemo = similarityDemo;
        this.chunkingDemo = chunkingDemo;
        this.miniRagDemo = miniRagDemo;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> demos = args.getOptionValues("demo");
        String demo = (demos == null || demos.isEmpty()) ? "similarity" : demos.get(0);

        System.out.println();
        switch (demo) {
            case "similarity" -> similarityDemo.run();
            case "chunking"   -> chunkingDemo.run();
            case "minirag"    -> miniRagDemo.run();
            default -> {
                System.out.println("알 수 없는 데모: " + demo);
                System.out.println("사용 가능: similarity | chunking | minirag");
            }
        }
        System.out.println();
    }
}
