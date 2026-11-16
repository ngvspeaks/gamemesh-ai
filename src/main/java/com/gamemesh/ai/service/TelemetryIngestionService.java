package com.gamemesh.ai.service;

import com.gamemesh.ai.model.dto.BatchTelemetryIngestResponse;
import com.gamemesh.ai.model.dto.TelemetryIngestRequest;
import com.gamemesh.ai.model.dto.TelemetryIngestResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TelemetryIngestionService {

    private final AmqpProducer amqpProducer;

    public TelemetryIngestionService(AmqpProducer amqpProducer) {
        this.amqpProducer = amqpProducer;
    }

    public TelemetryIngestResponse ingest(TelemetryIngestRequest request) {
        return amqpProducer.publishTelemetry(request);
    }

    public BatchTelemetryIngestResponse ingestBatch(List<TelemetryIngestRequest> requests) {
        return amqpProducer.publishBatch(requests);
    }
}
