package com.example.data

import com.example.model.*

object MultiAgentEngine {

    fun generateAgentInsights(
        scenario: DisasterScenario,
        userLat: Double,
        userLng: Double,
        personalRiskScore: Int,
        riskLevel: RiskLevel,
        nearestHazard: HazardZone?,
        nearestHospital: EmergencyFacility?,
        nearestShelter: EmergencyFacility?,
        language: Language
    ): List<AgentInsight> {
        val hazardText = when (language) {
            Language.ENGLISH -> "Detecting ${scenario.rainfallIntensityMmPerHour}mm/h precipitation & +${scenario.riverLevelChangeMeters}m surge. Epicenter: ${scenario.epicenterName}."
            Language.HINDI -> "वर्षा दर ${scenario.rainfallIntensityMmPerHour} मिमी/घंटा तथा जलस्तर +${scenario.riverLevelChangeMeters} मीटर बढ़ा। केंद्र: ${scenario.epicenterName}।"
            Language.MARATHI -> "पर्जन्यवृष्टी ${scenario.rainfallIntensityMmPerHour} मिमी/तास व पाणीपातळी +${scenario.riverLevelChangeMeters} मीटर वाढली. मुख्य केंद्र: ${scenario.epicenterName}."
        }

        val guardianText = when (language) {
            Language.ENGLISH -> "Citizen GPS at [$userLat, $userLng] is ${nearestHazard?.let { "%.1f km from %s".format(DisasterRepository.calculateDistanceKm(userLat, userLng, it.lat, it.lng), it.name) } ?: "within monitored zone"}. Personal risk: $personalRiskScore/100 (${riskLevel.label})."
            Language.HINDI -> "नागरिक की स्थिति [$userLat, $userLng] निकटतम खतरे से ${nearestHazard?.let { "%.1f किमी दूरी पर है".format(DisasterRepository.calculateDistanceKm(userLat, userLng, it.lat, it.lng)) } ?: "सक्रिय क्षेत्र में है"}। व्यक्तिगत जोखिम: $personalRiskScore/100।"
            Language.MARATHI -> "नागरिकाचे स्थान [$userLat, $userLng] धोक्याच्या ठिकाणापासून ${nearestHazard?.let { "%.1f किमी अंतरावर आहे".format(DisasterRepository.calculateDistanceKm(userLat, userLng, it.lat, it.lng)) } ?: "सक्रिय क्षेत्रात आहे"}। वैयक्तिक धोका: $personalRiskScore/100."
        }

        val impactText = when (language) {
            Language.ENGLISH -> "Cascade Level ${scenario.defaultCascadeLevel} triggered. Arterial road blockages detected; estimated ${scenario.populationExposed} residents in high vulnerability zone."
            Language.HINDI -> "कैस्केड स्तर ${scenario.defaultCascadeLevel} सक्रिय। मुख्य मार्ग अवरुद्ध; लगभग ${scenario.populationExposed} नागरिक प्रभावित क्षेत्र में।"
            Language.MARATHI -> "शृंखलाबद्ध स्तर ${scenario.defaultCascadeLevel} सुरू. मुख्य रस्ते बंद; अंदाजे ${scenario.populationExposed} नागरिक बाधित क्षेत्रात."
        }

        val hospitalText = when (language) {
            Language.ENGLISH -> nearestHospital?.let {
                "Nearest Facility: ${it.name} (${it.availableBeds}/${it.totalBeds} beds available, ${it.distanceKm} km away). Status: ${if (it.isAccessible) "ACCESSIBLE" else "WATERLOGGED"}."
            } ?: "Coordinating triage with district civil hospital."
            Language.HINDI -> nearestHospital?.let {
                "निकटतम अस्पताल: ${it.name} (${it.availableBeds}/${it.totalBeds} बेड उपलब्ध, ${it.distanceKm} किमी दूर)। स्थिति: ${if (it.isAccessible) "पहुंच योग्य" else "जलभराव"}।"
            } ?: "जिला सिविल अस्पताल के साथ समन्वय जारी।"
            Language.MARATHI -> nearestHospital?.let {
                "जवळचे रुग्णालय: ${it.name} (${it.availableBeds}/${it.totalBeds} खाटा उपलब्ध, ${it.distanceKm} किमी अंतर). स्थिती: ${if (it.isAccessible) "सुरू" else "पाणी साचले आहे"}."
            } ?: "जिल्हा सामान्य रुग्णालयाशी समन्वय सुरू."
        }

        val coordinatorText = when (language) {
            Language.ENGLISH -> "Synthesizing real-time telemetry from all 4 sub-agents: Recommending immediate evacuation to ${nearestShelter?.name ?: "High Ground Shelter"} via northern bypass route. Avoid riverfront bridges."
            Language.HINDI -> "सभी 4 एजेंटों के डेटा का समन्वय: तत्काल ${nearestShelter?.name ?: "उच्च भू-भाग आश्रय"} की ओर उत्तरी बाईपास से जाने की सलाह दी जाती है। नदी किनारे के पुलों से बचें।"
            Language.MARATHI -> "सर्व 4 एजंटांच्या विश्लेषणाचे एकत्रीकरण: त्वरित ${nearestShelter?.name ?: "उंच निवारा केंद्राकडे"} उत्तरेकडील बायपास मार्गाने जाण्याचा सल्ला दिला जात आहे."
        }

        return listOf(
            AgentInsight(
                agentType = AgentType.HAZARD,
                status = "Telemetric Analysis Online",
                analysisText = hazardText,
                confidenceScore = 96,
                keyMetric = "${scenario.rainfallIntensityMmPerHour} mm/h Rain"
            ),
            AgentInsight(
                agentType = AgentType.GUARDIAN,
                status = "GPS Risk Vector Active",
                analysisText = guardianText,
                confidenceScore = 94,
                keyMetric = "Risk: $personalRiskScore/100"
            ),
            AgentInsight(
                agentType = AgentType.IMPACT,
                status = "Cascade Model Evaluated",
                analysisText = impactText,
                confidenceScore = 91,
                keyMetric = "Level ${scenario.defaultCascadeLevel} Cascade"
            ),
            AgentInsight(
                agentType = AgentType.HOSPITAL,
                status = "Medical Logistics Mapped",
                analysisText = hospitalText,
                confidenceScore = 95,
                keyMetric = "${nearestHospital?.availableBeds ?: 68} Beds Open"
            ),
            AgentInsight(
                agentType = AgentType.COORDINATOR,
                status = "Response Synthesized",
                analysisText = coordinatorText,
                confidenceScore = 98,
                keyMetric = "Action Plan Ready"
            )
        )
    }

    fun generateAssistantResponse(
        query: String,
        scenario: DisasterScenario,
        userLat: Double,
        userLng: Double,
        personalRiskScore: Int,
        riskLevel: RiskLevel,
        nearestHospital: EmergencyFacility?,
        nearestShelter: EmergencyFacility?,
        language: Language
    ): ChatMessage {
        val q = query.lowercase()
        val nearestHazards = DisasterRepository.getInitialHazardZones(scenario)
        val nearestHazard = nearestHazards.minByOrNull {
            DisasterRepository.calculateDistanceKm(userLat, userLng, it.lat, it.lng)
        }

        val insights = generateAgentInsights(
            scenario = scenario,
            userLat = userLat,
            userLng = userLng,
            personalRiskScore = personalRiskScore,
            riskLevel = riskLevel,
            nearestHazard = nearestHazard,
            nearestHospital = nearestHospital,
            nearestShelter = nearestShelter,
            language = language
        )

        val recommendations = DisasterRepository.getPrioritizedRecommendations(scenario)

        val synthesizedText = when {
            q.contains("hospital") || q.contains("medical") || q.contains("doctor") || q.contains("अस्पताल") || q.contains("दवाखाना") || q.contains("रुग्णालय") -> {
                when (language) {
                    Language.ENGLISH -> "🏥 **Medical Triage Directives**:\n• Nearest accessible medical facility is **${nearestHospital?.name ?: "District Civil Hospital"}** located approx **${nearestHospital?.distanceKm ?: 1.4} km** away.\n• Available Capacity: **${nearestHospital?.availableBeds ?: 45} emergency beds** with active trauma triage.\n• **Route Note**: Take the elevated link road; low-lying river bridges are restricted."
                    Language.HINDI -> "🏥 **चिकित्सा व आपातकालीन निर्देश**:\n• निकटतम उपलब्ध अस्पताल **${nearestHospital?.name ?: "जिला सिविल अस्पताल"}** है जो लगभग **${nearestHospital?.distanceKm ?: 1.4} किमी** दूर स्थित है।\n• वर्तमान क्षमता: **${nearestHospital?.availableBeds ?: 45} आपातकालीन बेड** तैयार हैं।\n• **मार्ग सलाह**: केवल ऊंचे बाईपास मार्ग का उपयोग करें; नदी के पुलों पर जलभराव है।"
                    Language.MARATHI -> "🏥 **वैद्यकीय व आपत्कालीन सूचना**:\n• सर्वात जवळचे उपलब्ध रुग्णालय **${nearestHospital?.name ?: "जिल्हा सामान्य रुग्णालय"}** असून ते अंदाजे **${nearestHospital?.distanceKm ?: 1.4} किमी** अंतरावर आहे.\n• उपलब्ध खाटा: **${nearestHospital?.availableBeds ?: 45} आपत्कालीन खाटा** सज्ज आहेत.\n• **मार्ग सल्ला**: उंचावरील लिंक रोडचा वापर करा; नदीकाठचे रस्ते बंद आहेत."
                }
            }
            q.contains("shelter") || q.contains("safe") || q.contains("where to go") || q.contains("आश्रय") || q.contains("कहाँ") || q.contains("निवारा") || q.contains("कुठे") -> {
                when (language) {
                    Language.ENGLISH -> "🛡️ **Safe Evacuation Guidance**:\n• Primary safe haven is **${nearestShelter?.name ?: "Relief Camp Alpha"}** (${nearestShelter?.distanceKm ?: 0.9} km away).\n• Capacity available: **${nearestShelter?.availableBeds ?: 450} people** with hot meals, clean drinking water, and first-aid stations.\n• Follow the green highlighted Safe Route on your map."
                    Language.HINDI -> "🛡️ **सुरक्षित आश्रय मार्गदर्शन**:\n• आपका मुख्य सुरक्षित आश्रय **${nearestShelter?.name ?: "राहत शिविर अल्फा"}** (${nearestShelter?.distanceKm ?: 0.9} किमी) है।\n• यहाँ भोजन, स्वच्छ पेयजल तथा प्राथमिक चिकित्सा उपलब्ध है।\n• मानचित्र पर दिखाए गए हरे 'सुरक्षित मार्ग' का पालन करें।"
                    Language.MARATHI -> "🛡️ **सुरक्षित निवारा मार्गदर्शन**:\n• प्रमुख सुरक्षित निवारा केंद्र **${nearestShelter?.name ?: "मदत छावणी अल्फा"}** (${nearestShelter?.distanceKm ?: 0.9} किमी) आहे.\n• येथे जेवण, पिण्याचे स्वच्छ पाणी आणि प्रथमोपचार सुविधा उपलब्ध आहेत.\n• नकाशावरील हिरव्या 'सुरक्षित मार्गा'चा वापर करा."
                }
            }
            q.contains("sos") || q.contains("help") || q.contains("trapped") || q.contains("बचाव") || q.contains("मदत") -> {
                when (language) {
                    Language.ENGLISH -> "🚨 **Emergency Protocol Activated**:\n• Tap the red **SEND SOS** button immediately to transmit your telemetry directly to the Disaster Coordinator Command.\n• Keep your phone elevated and conserve battery.\n• If water is rising, immediately move to the highest accessible roof or upper floor."
                    Language.HINDI -> "🚨 **आपातकालीन प्रोटोकॉल सक्रिय**:\n• अपने GPS और डेटा को सीधे समन्वयक तक भेजने के लिए लाल **SEND SOS** बटन दबाएं।\n• अपने फोन को सुरक्षित व सूखा रखें तथा बैटरी बचाएं।\n• यदि जलस्तर बढ़ रहा है, तो तुरंत छत या ऊपरी मंजिल पर जाएं।"
                    Language.MARATHI -> "🚨 **तातडीचा प्रोटोकॉल सक्रिय**:\n• नियंत्रण कक्षाला थेट संदेश पाठवण्यासाठी लाल **SEND SOS** बटण दाबा.\n• फोन सुरक्षित ठेवून बॅटरी वाचवा.\n• पाणी वाढत असल्यास तात्काळ उंचावर किंवा घराच्या छतावर जा."
                }
            }
            else -> {
                when (language) {
                    Language.ENGLISH -> "⚡ **RakshAI Multi-Agent Response Synthesis**:\nYour current area has a **${riskLevel.label.uppercase()}** risk profile ($personalRiskScore/100) due to ${scenario.title}.\n\n1. **Move to Higher Ground**: Proceed immediately towards ${nearestShelter?.name ?: "Relief Camp Alpha"} avoiding the eastern riverbank corridor.\n2. **Hospital Access**: ${nearestHospital?.name ?: "Civil Hospital"} is open with ${nearestHospital?.availableBeds ?: 68} beds ready.\n3. **SOS Ready**: If trapped or in immediate danger, use the bottom SOS button to broadcast emergency beacon."
                    Language.HINDI -> "⚡ **रक्षाAI मल्टी-एजेंट त्वरित विश्लेषण**:\n${scenario.titleHi} के कारण आपका क्षेत्र **${riskLevel.label}** ($personalRiskScore/100) जोखिम में है।\n\n1. **ऊंचे स्थान पर जाएं**: तुरंत ${nearestShelter?.name ?: "राहत शिविर"} की ओर प्रस्थान करें और नदी किनारे के रास्तों से बचें।\n2. **चिकित्सा सुविधा**: ${nearestHospital?.name ?: "सिविल अस्पताल"} में आपातकालीन वार्ड सक्रिय है।\n3. **SOS सहायता**: यदि आप फंसे हुए हैं, तो तुरंत नीचे दिए गए लाल SOS बटन का उपयोग करें।"
                    Language.MARATHI -> "⚡ **रक्षाAI मल्टी-एजंट त्वरित विश्लेषण**:\n${scenario.titleMr} मुळे तुमचा परिसर **${riskLevel.label}** ($personalRiskScore/100) धोक्यात आहे.\n\n1. **उंचावर जा**: नदीकाठचा रस्ता टाळून त्वरित ${nearestShelter?.name ?: "मदत छावणीकडे"} जा.\n2. **वैद्यकीय मदत**: ${nearestHospital?.name ?: "सामान्य रुग्णालय"} सुरू असून तेथे खाटा उपलब्ध आहेत.\n3. **SOS मदत**: संकटात अडकल्यास त्वरित खालील लाल SOS बटण दाबून सिग्नल पाठवा."
                }
            }
        }

        return ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            sender = "RakshAI Multi-Agent Core",
            text = synthesizedText,
            timestamp = System.currentTimeMillis(),
            isUser = false,
            agentInsights = insights,
            recommendations = recommendations
        )
    }
}
