package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.AdvisedRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.RequestResponseAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({ "local-env", "local" })
class DemoApplicationTests {

	private static final Logger log = LoggerFactory.getLogger(DemoApplicationTests.class);

	@Autowired
	private ChatModel chatModel;
	@Autowired
	private EmbeddingModel embeddingModel;

	@Value("classpath:documents/story1.md")
	Resource textFile1;

	@Test
	@DisplayName("簡單的回覆")
	void testSimpleReply() {
		String prompt = "講個笑話";

		String response = chatModel.call(prompt);

		log.info("回應結果: {}", response);
	}

	@Test
	@DisplayName("使用 ChatClient 的簡單回覆")
	void testChatClientSimpleReply() {
		ChatClient chatClient = ChatClient.builder(chatModel).build();
		String prompt = "講個笑話";
		String response = chatClient.prompt().user(prompt).call().content();
		log.info("回應結果: {}", response);
	}

	@Test
	@DisplayName("使用帶記憶功能的 ChatClient 進行對話")
	void testChatClientWithMemory() {
		InMemoryChatMemory chatMemory = new InMemoryChatMemory();
		ChatClient chatClient = ChatClient.builder(chatModel)
				.defaultAdvisors(List.of(new MessageChatMemoryAdvisor(chatMemory)))
				.build();

		// 第一次對話
		String prompt1 = "OpenAI 是做什麼的公司?";
		ChatResponse chatResponse = chatClient.prompt().user(prompt1).call().chatResponse();
		log.info("第一次回應結果: {}", chatResponse.getResult().getOutput().getContent());

		// 第二次對話
		String prompt2 = "他們的使命是什麼?";
		chatResponse = chatClient.prompt().user(prompt2).call().chatResponse();
		log.info("第二次回應結果: {}", chatResponse.getResult().getOutput().getContent());
	}

	@Test
	@DisplayName("使用帶變數的 PromptTemplate 生成對話") // https://docs.spring.io/spring-ai/reference/api/prompt.html#_example_usage
	void testPromptTemplateWithVariables() {
		// 建立提示模板
		PromptTemplate promptTemplate = new PromptTemplate("Tell me a {adjective} joke about {topic}");

		// 使用變數產生提示
		Prompt prompt = promptTemplate.create(Map.of("adjective", "有趣的", "topic", "貓"));

		String response = chatModel.call(prompt).getResult().getOutput().getContent();
		log.info("回應結果: {}", response);
	}

	@Test
	@DisplayName("將用戶請求分類為特定類別") // https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html#_structured_output_api
	void testUserRequestClassification() {
		ChatClient chatClient = ChatClient.builder(chatModel).build();
		UserRequest userRequest = chatClient
				.prompt()
				.user(userSpec -> userSpec.text("""
						請將用戶請求分類為以下類別之一：
						1. Product Inquiry
						2. Shipping Issue
						3. Other

						--------------
						內容：
						{text}
						--------------
						""")
						.param("text", "請問今天天氣如何?"))
				.call()
				.entity(UserRequest.class);
		log.info("用戶請求分類結果: {}", userRequest.type());
	}

	@Test
	@DisplayName("使用 FunctionCallbackWrapper 查詢多個城市的天氣") // https://docs.spring.io/spring-ai/reference/api/chat/functions/openai-chat-functions.html#_registering_functions_as_beans
	void testWithFunctionCall() {
		UserMessage userMessage = new UserMessage("舊金山、東京和巴黎的天氣如何？");

		// 註冊天氣查詢服務
		@SuppressWarnings("rawtypes")
		FunctionCallbackWrapper currentWeather = FunctionCallbackWrapper.builder(new MockWeatherService())
				.withName("CurrentWeatherService")
				.withDescription("獲取指定地點的天氣資訊")
				.build();

		// 設置提示選項
		var promptOptions = OpenAiChatOptions.builder().withFunctionCallbacks(List.of(currentWeather)).build();

		// 呼叫 ChatModel 並獲取回應
		ChatResponse response = chatModel.call(new Prompt(List.of(userMessage), promptOptions));

		// 紀錄並顯示回應結果（中文）
		log.info("天氣查詢結果: {}", response.getResult().getOutput().getContent());
	}

	@Test
	@DisplayName("測試嵌入模型的回應")
	void testEmbeddingModelResponse() {
		EmbeddingResponse embeddingResponse = embeddingModel
				.embedForResponse(List.of("Hello World", "World is big and salvation is near"));
		log.info("嵌入模型回應: {}", embeddingResponse);
	}

	@Test
	@DisplayName("測試文件處理和向量儲存")
	void testDocumentProcessingAndVectorStore() {
		var textReader1 = new TextReader(textFile1);
		List<Document> documents = new ArrayList<>();
		documents.addAll(textReader1.get());
		log.info("初始文件數量: {}", documents.size());

		VectorStore vectorStore = new SimpleVectorStore(embeddingModel);
		TokenTextSplitter tokenTextSplitter = new TokenTextSplitter(200, 350, 5, 10000, true);
		documents = tokenTextSplitter.split(documents);
		log.info("分割後的文件數量: {}", documents.size());

		vectorStore.add(documents);
	}

	@Test
	@DisplayName("測試文件處理、向量儲存和相似性搜尋")
	void testDocumentProcessingVectorStoreAndSimilaritySearch() {
		var textReader1 = new TextReader(textFile1);
		List<Document> documents = new ArrayList<>();
		documents.addAll(textReader1.get());
		log.info("初始文件數量: {}", documents.size());

		VectorStore vectorStore = new SimpleVectorStore(embeddingModel);
		TokenTextSplitter tokenTextSplitter = new TokenTextSplitter(200, 350, 5, 10000, true);
		documents = tokenTextSplitter.split(documents);
		log.info("分割後的文件數量: {}", documents.size());

		vectorStore.add(documents);

		List<Document> similarDocuments = vectorStore.similaritySearch(
				SearchRequest.query("這次去了東京哪些景點").withTopK(5));
		log.info("相似文件: {}", similarDocuments);
	}

	@Test
	@DisplayName("測試自定義請求和回應顧問")
	void testCustomRequestResponseAdvisors() {
		ChatClient chatClient = ChatClient.builder(chatModel).defaultAdvisors(new RequestResponseAdvisor() {
			@Override
			public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
				log.info("自定義請求: " + request.userText());
				return request;
			}

			@Override
			public ChatResponse adviseResponse(ChatResponse response, Map<String, Object> context) {
				log.info("回應結果: " + response);
				return response;
			}
		}).build();

		String responseContent = chatClient.prompt().user("講個寓言故事").call().content();
		log.info("最終回應內容: {}", responseContent);
	}

}
