package com.example.demo.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class AICategorizationService {
    
    @Value("${ai.categorization.enabled:true}")
    private boolean categorizationEnabled;
    
    @Value("${ai.categorization.confidence-threshold:0.7}")
    private double confidenceThreshold;
    
    @Value("${ml.service.enabled:true}")
    private boolean mlServiceEnabled;
    
    private final org.springframework.web.reactive.function.client.WebClient mlWebClient;
    private final TranslationService translationService;
    
    public AICategorizationService(org.springframework.web.reactive.function.client.WebClient mlWebClient,
                                   TranslationService translationService) {
        this.mlWebClient = mlWebClient;
        this.translationService = translationService;
    }
    
    public GrievanceCategorizationResult categorizeGrievance(String title, String description) {
        // Translate to English if needed (auto when enabled)
        String titleEn = translationService.translateToEnglish(title, null);
        String descEn = translationService.translateToEnglish(description, null);
        if (!categorizationEnabled) {
            return new GrievanceCategorizationResult("OTHER", 0.0);
        }
        // Try ML service first
        if (mlServiceEnabled) {
            try {
                MlRequest req = new MlRequest(titleEn, descEn);
                MlResponse resp = mlWebClient.post()
                        .uri("/predict")
                        .bodyValue(req)
                        .retrieve()
                        .bodyToMono(MlResponse.class)
                        .block();
                if (resp != null && resp.category != null) {
                    GrievanceCategorizationResult ml = new GrievanceCategorizationResult(resp.category.toUpperCase(), resp.confidence);
                    // If ML is uncertain, try keywords and prefer the stronger one
                    if (ml.confidence() < 0.6) {
                        GrievanceCategorizationResult kw = categorizeByKeywords(title, description);
                        if (kw.confidence() >= ml.confidence()) {
                            return kw;
                        }
                    }
                    return ml;
                }
            } catch (Exception e) {
                log.warn("ML service unavailable, falling back to keywords: {}", e.getMessage());
            }
        }
        // Fallback to keywords
        try {
            return categorizeByKeywords(titleEn, descEn);
        } catch (Exception e) {
            log.error("Error during fallback categorization: {}", e.getMessage(), e);
            return new GrievanceCategorizationResult("OTHER", 0.0);
        }
    }

    public java.util.List<GrievanceCategorizationResult> rankCategories(String title, String description) {
        // Base on keyword scores, optionally nudge ML category
        String text = ((title == null ? "" : title) + " " + (description == null ? "" : description)).toLowerCase();
        String[] rawTokens = text.split("[^a-zA-Z]+");
        java.util.Set<String> tokens = new java.util.HashSet<>();
        for (String t : rawTokens) { if (!t.isBlank()) tokens.add(t); }

        String[][] categoryKeywords = getCategoryKeywordMatrix();

        java.util.Map<String,Integer> scoreMap = new java.util.HashMap<>();
        for (String[] entry : categoryKeywords) {
            String category = entry[0];
            int score = 0;
            for (int i = 1; i < entry.length; i++) {
                if (tokens.contains(entry[i])) score++;
            }
            scoreMap.put(category, score);
        }

        // Try ML and gently boost its category
        try {
            if (mlServiceEnabled) {
                MlRequest req = new MlRequest(title, description);
                MlResponse resp = mlWebClient.post().uri("/predict").bodyValue(req).retrieve().bodyToMono(MlResponse.class).block();
                if (resp != null && resp.category != null) {
                    String mlCat = resp.category.toUpperCase();
                    scoreMap.put(mlCat, scoreMap.getOrDefault(mlCat, 0) + (resp.confidence > 0.6 ? 2 : 1));
                }
            }
        } catch (Exception e) {
            log.debug("ML ranking hint unavailable: {}", e.getMessage());
        }

        java.util.List<GrievanceCategorizationResult> ranked = new java.util.ArrayList<>();
        for (var e : scoreMap.entrySet()) {
            int s = e.getValue();
            double confidence = switch (s) { case 0 -> 0.5; case 1 -> 0.7; case 2 -> 0.82; default -> 0.9; };
            ranked.add(new GrievanceCategorizationResult(e.getKey(), confidence));
        }
        ranked.sort((a,b) -> Double.compare(b.confidence(), a.confidence()));
        return ranked;
    }

    private String[][] getCategoryKeywordMatrix() {
        return new String[][]{
            {"INFRASTRUCTURE",
                "road","bridge","building","street","pothole","streetlight","light","infrastructure","construction","footpath","culvert",
                "sadak","pul","imarat","gadda","battee","nali",
                "saalai","paalam","veedhi","mali","vilakku","kuzhai",
                "dari","sethu","streetu","gadda","deepam","kalva"
            },
            {"HEALTHCARE",
                "hospital","doctor","medical","health","medicine","clinic","ambulance","ward","nurse",
                "aspatal","chikitsa","davakhana",
                "maruthuvamanai","vaidyan","aasupatri",
                "aasupatri","chikitsa","aushadhi"
            },
            {"EDUCATION",
                "school","college","education","teacher","student","exam","scholarship","bus","uniform",
                "schooli","vidyalaya","shiksha","adhyapak","chhatra",
                "palli","kaloori","aaseeriyar","maanavar",
                "paathashala","vidya","upaadhyaya","vidyarthi"
            },
            {"TRANSPORTATION",
                "transport","bus","train","traffic","parking","vehicle","metro","auto","rickshaw","ticket",
                "yatayat","bas","rail","jam","park","gaadi",
                "saadagam","rail","natpu","gaadi",
                "pravahana","railway","parku","vahana"
            },
            {"UTILITIES",
                "water","electricity","power","gas","utility","supply","sewage","drainage","pipeline","leak","power","cut","load","shedding",
                "pani","bijli","gas","nal","nali","paip","rasavadi",
                "thanneer","minveli","anilai","paippu","ottam","neruppu",
                "neellu","current","gasu","paipu","leakage"
            },
            {"ENVIRONMENT",
                "garbage","waste","pollution","environment","clean","dirty","trash","sewage","dump","smell","mosquito",
                "kachra","kuda","pradushan","safai","ganda","machhar",
                "kuppai","kazhu","kalusham","sutham","asutham","kosu",
                "kacharam","kalushita","pacha","mosquito"
            },
            {"SAFETY_SECURITY",
                "police","crime","safety","security","emergency","theft","robbery","harassment","violence","accident",
                "police","aparadh","suraksha","chori","lut","hinsaa",
                "kaval","kolai","bathirapu","kolai","balatkar",
                "police","donga","bhadrata","theft","apghat"
            },
            {"HOUSING",
                "house","housing","property","rent","slum","residence","encroachment","illegal construction","land",
                "ghar","makaan","kiraya","jhuggi","awas","zamin",
                "veedu","vasathi","maanai","manai","bhoomi",
                "illu","nivasam","rentu","bhumi"
            },
            {"EMPLOYMENT",
                "job","employment","work","salary","unemployment","labor","wage","contract","transfer","promotion",
                "naukri","rojgar","vetan","birozgar","mazdoor",
                "velai","uzhaippu","sambalam","veli ilamai",
                "udyogam","pani","vetanam","nirudyoga"
            }
        };
    }

    private static class MlRequest {
        public String title;
        public String description;
        public MlRequest(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }
    private static class MlResponse {
        public String category;
        public double confidence;
    }
    
    private GrievanceCategorizationResult categorizeByKeywords(String title, String description) {
        String text = ((title == null ? "" : title) + " " + (description == null ? "" : description)).toLowerCase();
        String[] rawTokens = text.split("[^a-zA-Z]+");
        java.util.Set<String> tokens = new java.util.HashSet<>();
        for (String t : rawTokens) { if (!t.isBlank()) tokens.add(t); }

        String[][] categoryKeywords = new String[][]{
            // INFRASTRUCTURE
            {"INFRASTRUCTURE",
                "road","bridge","building","street","pothole","streetlight","light","infrastructure","construction","footpath","culvert",
                // Hindi
                "sadak","pul","imarat","gadda","battee","nali",
                // Tamil (latin)
                "saalai","paalam","veedhi","mali","vilakku","kuzhai",
                // Telugu (latin)
                "dari","sethu","streetu","gadda","deepam","kalva"
            },
            // HEALTHCARE
            {"HEALTHCARE",
                "hospital","doctor","medical","health","medicine","clinic","ambulance","ward","nurse",
                "aspatal","chikitsa","davakhana",
                "maruthuvamanai","vaidyan","aasupatri",
                "aasupatri","chikitsa","aushadhi"
            },
            // EDUCATION
            {"EDUCATION",
                "school","college","education","teacher","student","exam","scholarship","bus","uniform",
                "schooli","vidyalaya","shiksha","adhyapak","chhatra",
                "palli","kaloori","aaseeriyar","maanavar",
                "paathashala","vidya","upaadhyaya","vidyarthi"
            },
            // TRANSPORTATION
            {"TRANSPORTATION",
                "transport","bus","train","traffic","parking","vehicle","metro","auto","rickshaw","ticket",
                "yatayat","bas","rail","jam","park","gaadi",
                "saadagam","rail","natpu","gaadi",
                "pravahana","railway","parku","vahana"
            },
            // UTILITIES
            {"UTILITIES",
                "water","electricity","power","gas","utility","supply","sewage","drainage","pipeline","leak","power","cut","load","shedding",
                "pani","bijli","gas","nal","nali","paip","rasavadi",
                "thanneer","minveli","anilai","paippu","ottam","neruppu",
                "neellu","current","gasu","paipu","leakage"
            },
            // ENVIRONMENT
            {"ENVIRONMENT",
                "garbage","waste","pollution","environment","clean","dirty","trash","sewage","dump","smell","mosquito",
                "kachra","kuda","pradushan","safai","ganda","machhar",
                "kuppai","kazhu","kalusham","sutham","asutham","kosu",
                "kacharam","kalushita","pacha","mosquito"
            },
            // SAFETY_SECURITY
            {"SAFETY_SECURITY",
                "police","crime","safety","security","emergency","theft","robbery","harassment","violence","accident",
                "police","aparadh","suraksha","chori","lut","hinsaa",
                "kaval","kolai","bathirapu","kolai","balatkar",
                "police","donga","bhadrata","theft","apghat"
            },
            // HOUSING
            {"HOUSING",
                "house","housing","property","rent","slum","residence","encroachment","illegal construction","land",
                "ghar","makaan","kiraya","jhuggi","awas","zamin",
                "veedu","vasathi","maanai","manai","bhoomi",
                "illu","nivasam","rentu","bhumi"
            },
            // EMPLOYMENT
            {"EMPLOYMENT",
                "job","employment","work","salary","unemployment","labor","wage","contract","transfer","promotion",
                "naukri","rojgar","vetan","birozgar","mazdoor",
                "velai","uzhaippu","sambalam","veli ilamai",
                "udyogam","pani","vetanam","nirudyoga"
            }
        };

        String chosen = "OTHER";
        int bestScore = 0;
        java.util.Set<String> strongUtilities = java.util.Set.of("drainage","sewage","leak","pipeline","water","electricity","power");
        java.util.Set<String> strongEnvironment = java.util.Set.of("garbage","waste","pollution","trash","dump","mosquito");
        for (String[] entry : categoryKeywords) {
            String category = entry[0];
            int score = 0;
            for (int i = 1; i < entry.length; i++) {
                if (tokens.contains(entry[i])) score++;
            }
            if (score > bestScore) {
                bestScore = score;
                chosen = category;
            }
        }

        // Strong signal overrides for common ambiguities (e.g., drainage -> UTILITIES)
        if (bestScore > 0) {
            if (!java.util.Collections.disjoint(tokens, strongUtilities)) {
                chosen = "UTILITIES";
            } else if (!java.util.Collections.disjoint(tokens, strongEnvironment)) {
                chosen = "ENVIRONMENT";
            }
        }

        double confidence = switch (bestScore) {
            case 0 -> 0.5; // unknown
            case 1 -> 0.7;
            case 2 -> 0.82;
            default -> 0.9;
        };

        return new GrievanceCategorizationResult(chosen, confidence);
    }
    
    
    public boolean shouldAutoAssign(GrievanceCategorizationResult result) {
        return result.confidence() >= confidenceThreshold;
    }
    
    public record GrievanceCategorizationResult(String category, double confidence) {}
}
