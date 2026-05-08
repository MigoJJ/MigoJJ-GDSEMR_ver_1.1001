package com.emr.gds.context;

import com.emr.gds.repository.sqlite.SqliteAbbreviationRepository;
import com.emr.gds.repository.sqlite.SqlitePlanHistoryRepository;
import com.emr.gds.repository.sqlite.SqliteProblemRepository;
import com.emr.gds.repository.sqlite.SqliteReferenceRepository;
import com.emr.gds.service.AbbreviationService;
import com.emr.gds.service.PlanHistoryService;
import com.emr.gds.service.ProblemListService;
import com.emr.gds.service.ReferenceService;
import com.emr.gds.core.db.AppDatabaseManager;
import com.emr.gds.infrastructure.ai.AiAssistantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized context for application services and shared state.
 */
public class AppContext {
    private static final Logger logger = LoggerFactory.getLogger(AppContext.class);
    private static final AppContext INSTANCE = new AppContext();

    private final Map<String, String> abbrevMap = new HashMap<>();
    private final AbbreviationService abbreviationService;
    private final ProblemListService problemListService;
    private final PlanHistoryService planHistoryService;
    private final ReferenceService referenceService;
    private final AiAssistantService aiAssistantService;

    private AppContext() {
        logger.info("Initializing AppContext...");
        this.abbreviationService = new AbbreviationService(new SqliteAbbreviationRepository(), abbrevMap);
        this.problemListService = new ProblemListService(new SqliteProblemRepository());
        this.planHistoryService = new PlanHistoryService(new SqlitePlanHistoryRepository());
        this.referenceService = new ReferenceService(new SqliteReferenceRepository(AppDatabaseManager.getInstance()));
        this.aiAssistantService = new AiAssistantService();
    }

    public static AppContext getInstance() {
        return INSTANCE;
    }

    public Map<String, String> getAbbrevMap() {
        return abbrevMap;
    }

    public AbbreviationService getAbbreviationService() {
        return abbreviationService;
    }

    public ProblemListService getProblemListService() {
        return problemListService;
    }

    public PlanHistoryService getPlanHistoryService() {
        return planHistoryService;
    }

    public ReferenceService getReferenceService() {
        return referenceService;
    }

    public AiAssistantService getAiAssistantService() {
        return aiAssistantService;
    }
}
