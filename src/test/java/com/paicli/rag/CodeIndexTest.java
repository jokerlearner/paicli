package com.paicli.rag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CodeIndexTest {

    @TempDir
    Path tempDir;

    private String previousRagDir;
    private EmbeddingClient embeddingClient;

    @BeforeEach
    void setUp() {
        previousRagDir = System.getProperty("paicli.rag.dir");
        System.setProperty("paicli.rag.dir", tempDir.resolve("rag-store").toString());
        embeddingClient = new EmbeddingClient("ollama", "stub", "http://localhost", "") {
            @Override
            public float[] embed(String text) {
                return new float[]{1.0f, 0.0f};
            }
        };
    }

    @AfterEach
    void tearDown() {
        if (previousRagDir == null) {
            System.clearProperty("paicli.rag.dir");
        } else {
            System.setProperty("paicli.rag.dir", previousRagDir);
        }
    }

    @Test
    void testIndexNonExistentPath() {
        CodeIndex indexer = new CodeIndex(embeddingClient);
        CodeIndex.IndexResult result = indexer.index("/non/existent/path");
        assertEquals(0, result.chunkCount());
        assertTrue(result.message().contains("路径不存在"));
    }

    @Test
    void testIndexCurrentProject() {
        CodeIndex indexer = new CodeIndex(embeddingClient);
        // 索引测试资源目录
        CodeIndex.IndexResult result = indexer.index(testResourceRoot());
        assertTrue(result.chunkCount() > 0, "应该至少索引一个代码块");
        assertTrue(result.message().contains("索引完成"));
    }

    @Test
    void reportsProgressThroughListener() {
        List<String> messages = new ArrayList<>();
        CodeIndex indexer = new CodeIndex(embeddingClient, messages::add);

        CodeIndex.IndexResult result = indexer.index(testResourceRoot());

        assertTrue(result.chunkCount() > 0, "应该至少索引一个代码块");
        assertTrue(messages.stream().anyMatch(message -> message.startsWith("🔍 开始索引")));
        assertTrue(messages.stream().anyMatch(message -> message.startsWith("📁 发现")));
        assertTrue(messages.stream().anyMatch(message -> message.startsWith("✅ 索引完成")));
    }

    private String testResourceRoot() {
        return Path.of("src", "test", "resources", "rag").toAbsolutePath().normalize().toString();
    }
}
