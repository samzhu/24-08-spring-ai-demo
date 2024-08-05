# Spring AI Demo 專案

這是一個示範如何使用 Spring AI 與 OpenAI 進行整合的專案。本專案展示了如何設置和使用 OpenAI API 來實現人工智慧功能。

## 環境設定

在開始之前，請確保您已經在 `src/main/resources/application.yml` 文件中配置了您的 OpenAI API Key。

### 步驟

1. 複製此專案到您的本地環境：
    ```bash
    git clone https://github.com/samzhu/24-08-spring-ai-demo.git
    cd 24-08-spring-ai-demo
    ```

2. 打開 `src/main/resources/application.yml` 文件，並添加您的 OpenAI API Key：

    ```yaml
    spring:
      ai:
        openai:
          api-key: sk-xxx-oooooooo
    ```

## 前置需求

在運行此專案之前，請確保您的環境中已安裝以下軟體：

- JDK 21 或更高版本

## 使用方法


在專案目錄下，使用 Gradle 編譯並運行專案：

``` bash
./gradlew test --tests "com.example.demo.DemoApplicationTests.testSimpleReply"
```

## 範例功能

本專案展示了以下功能：

1. **簡單的回覆**：
   - 說明：使用 `ChatModel` 呼叫簡單的文本生成功能。
   - 測試方法：`testSimpleReply`

2. **使用 ChatClient 的簡單回覆**：
   - 說明：使用 `ChatClient` 呼叫簡單的文本生成功能。
   - 測試方法：`testChatClientSimpleReply`

3. **使用帶記憶功能的 ChatClient 進行對話**：
   - 說明：使用 `InMemoryChatMemory` 實現對話記憶功能。
   - 測試方法：`testChatClientWithMemory`

4. **使用帶變數的 PromptTemplate 生成對話**：
   - 說明：利用 `PromptTemplate` 和變數生成自定義對話提示。
   - 測試方法：`testPromptTemplateWithVariables`

5. **將用戶請求分類為特定類別**：
   - 說明：利用 `ChatClient` 將用戶請求分類為預定義的類別。
   - 測試方法：`testUserRequestClassification`

6. **使用 FunctionCallbackWrapper 查詢多個城市的天氣**：
   - 說明：使用 `FunctionCallbackWrapper` 註冊並查詢多個城市的天氣資訊。
   - 測試方法：`testWithFunctionCall`

7. **測試嵌入模型的回應**：
   - 說明：利用 `EmbeddingModel` 生成文本嵌入並回應。
   - 測試方法：`testEmbeddingModelResponse`

8. **測試文件處理和向量儲存**：
   - 說明：使用 `TextReader` 讀取文件，並利用 `TokenTextSplitter` 進行分割，最後儲存到 `VectorStore` 中。
   - 測試方法：`testDocumentProcessingAndVectorStore`

9. **測試文件處理、向量儲存和相似性搜尋**：
   - 說明：除了文件處理和向量儲存，還進行相似性搜尋。
   - 測試方法：`testDocumentProcessingVectorStoreAndSimilaritySearch`

10. **測試自定義請求和回應顧問**：
    - 說明：使用自定義的 `RequestResponseAdvisor` 進行請求和回應的顧問功能。
    - 測試方法：`testCustomRequestResponseAdvisors`
