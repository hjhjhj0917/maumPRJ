package com.example.maum.scheduler;

import com.example.maum.repository.SyncOutboxRepository;
import com.example.maum.repository.entity.SyncOutboxEntity;
import com.example.maum.service.IGcsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

// DB 없이 outbox 작업 종류별 처리와 성공(삭제)/실패(attempts 증가) 분기를 확인함. 락·백오프 쿼리는 DiaryDbIntegrationTest 가 확인함
class SyncOutboxWorkerTest {

    SyncOutboxRepository repository = mock(SyncOutboxRepository.class);
    IGcsService gcsService = mock(IGcsService.class);
    MockRestServiceServer server;
    SyncOutboxWorker worker;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        worker = new SyncOutboxWorker(repository, gcsService, builder.build());
        ReflectionTestUtils.setField(worker, "pythonApiUrl", "http://ai-server");
    }

    private SyncOutboxEntity row(String target, String action, String refKey) {
        return SyncOutboxEntity.builder().id(1L).target(target).action(action).refKey(refKey).build();
    }

    private void claim(SyncOutboxEntity... rows) {
        when(repository.claimDue(SyncOutboxWorker.MAX_ATTEMPTS, SyncOutboxWorker.BATCH_SIZE)).thenReturn(List.of(rows));
    }

    @Test
    void 스토리지_파일_삭제를_처리하고_행을_지운다() {
        SyncOutboxEntity row = row("STORAGE", "DELETE", "diary/x/a.png");
        claim(row);

        worker.processOutbox();

        verify(gcsService).deleteImage("diary/x/a.png");
        verify(repository).delete(row);
    }

    @Test
    void 벡터_삭제를_AI_서버에_요청하고_성공하면_행을_지운다() {
        SyncOutboxEntity row = row("MONGO", "PURGE_USER", "user-public-id");
        claim(row);
        server.expect(requestTo("http://ai-server/api/vector/delete"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.action").value("PURGE_USER"))
                .andExpect(jsonPath("$.ref_key").value("user-public-id"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        worker.processOutbox();

        server.verify();
        verify(repository).delete(row);
    }

    @Test
    void AI_서버가_실패하면_행을_남기고_시도_횟수만_올린다() {
        SyncOutboxEntity row = row("MONGO", "DELETE", "diary-public-id");
        claim(row);
        server.expect(requestTo("http://ai-server/api/vector/delete")).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        worker.processOutbox();

        verify(repository, never()).delete(row);
        assertEquals((short) 1, row.getAttempts().shortValue());
    }

    @Test
    void 지원하지_않는_작업은_삭제하지_않고_실패로_센다() {
        SyncOutboxEntity row = row("STORAGE", "PURGE_USER", "x");
        claim(row);

        worker.processOutbox();

        verify(repository, never()).delete(row);
        assertEquals((short) 1, row.getAttempts().shortValue());
    }
}
