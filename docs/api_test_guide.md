# APIテスト手順

本ドキュメントは、著者・書籍リソースのバリデーションを含むAPIテスト手順をまとめたものです。

## 1. 著者登録（POST /authors）

### 正常系
1. 名前、生年月日が正しく設定されている場合
   ```bash
   curl -X POST -H "Content-Type: application/json" -d '{"name": "テスト著者", "birthDate": "1990-01-01"}' http://localhost:8080/authors
   ```
   - 期待結果: 200 OK
   - レスポンス: 登録された著者の情報（IDは自動採番）

### 異常系
1. 名前が空の場合
   ```bash
   curl -X POST -H "Content-Type: application/json" -d '{"name": "", "birthDate": "1990-01-01"}' http://localhost:8080/authors
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "VALIDATION_ERROR"
   - エラーメッセージ: "名前は必須です"

2. 生年月日が未来の日付の場合
   ```bash
   curl -X POST -H "Content-Type: application/json" -d '{"name": "未来の著者", "birthDate": "2030-01-01"}' http://localhost:8080/authors
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "VALIDATION_ERROR"
   - エラーメッセージ: "生年月日は過去の日付である必要があります"

3. 同じ名前・生年月日の著者が既に存在する場合
   ```bash
   curl -X POST -H "Content-Type: application/json" -d '{"name": "テスト著者", "birthDate": "1990-01-01"}' http://localhost:8080/authors
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "DUPLICATE_AUTHOR"
   - エラーメッセージ: "同じ名前・生年月日の著者が既に存在します"

## 2. 著者更新（PUT /authors）

### 正常系
1. 存在する著者IDで、有効な更新内容が指定された場合
   ```bash
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "name": "更新された著者", "birthDate": "1980-01-01"}' http://localhost:8080/authors
   ```
   - 期待結果: 200 OK
   - レスポンス: 更新された著者の情報

### 異常系
1. 存在しない著者IDが指定された場合
   ```bash
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 999, "name": "更新テスト", "birthDate": "1980-01-01"}' http://localhost:8080/authors
   ```
   - 期待結果: 404 Not Found
   - エラーコード: "AUTHOR_NOT_FOUND"
   - エラーメッセージ: "著者ID 999 は存在しません"

2. 名前が空の場合
   ```bash
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "name": "", "birthDate": "1980-01-01"}' http://localhost:8080/authors
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "VALIDATION_ERROR"
   - エラーメッセージ: "名前は必須です"

3. 生年月日が未来の日付の場合
   ```bash
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "name": "更新テスト", "birthDate": "2030-01-01"}' http://localhost:8080/authors
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "VALIDATION_ERROR"
   - エラーメッセージ: "生年月日は過去の日付である必要があります"

## 3. 書籍登録（POST /books）

### 正常系
1. タイトル、価格、ステータス、著者IDが正しく設定されている場合
   ```bash
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": 1000, "status": "unpublished", "authorIds": [1]}' http://localhost:8080/books
   ```
   - 期待結果: 200 OK
   - レスポンス: 登録された書籍の情報（IDは自動採番）

2. 同じタイトルでも著者の組み合わせが異なる場合は登録できること
   > 注意: このテストを実行する前に、著者ID 1, 2, 3が存在していることを確認してください。
   ```bash
   # 最初の書籍を登録
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": 1000, "status": "unpublished", "authorIds": [1, 2]}' http://localhost:8080/books

   # 同じタイトルでも著者の組み合わせが異なる書籍を登録
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": 2000, "status": "unpublished", "authorIds": [1, 3]}' http://localhost:8080/books
   ```
   - 期待結果: 200 OK
   - レスポンス: 登録された書籍の情報（IDは自動採番）

### 異常系
1. タイトルが空の場合
   ```bash
   curl -X POST -H "Content-Type: application/json" -d '{"title": "", "price": 1000, "status": "unpublished", "authorIds": [1]}' http://localhost:8080/books
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "VALIDATION_ERROR"
   - エラーメッセージ: "タイトルは必須です"

2. 価格が負の数の場合
   ```bash
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": -1000, "status": "unpublished", "authorIds": [1]}' http://localhost:8080/books
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "VALIDATION_ERROR"
   - エラーメッセージ: "価格は0以上である必要があります"

3. ステータスが不正な場合
   ```bash
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": 1000, "status": "invalid_status", "authorIds": [1]}' http://localhost:8080/books
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "VALIDATION_ERROR"
   - エラーメッセージ: "ステータスはunpublishedまたはpublishedである必要があります"

4. 著者IDが指定されていない場合
   ```bash
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": 1000, "status": "unpublished", "authorIds": []}' http://localhost:8080/books
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "VALIDATION_ERROR"
   - エラーメッセージ: "著者は最低1人必要です"

5. 存在しない著者IDが指定された場合
   ```bash
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": 1000, "status": "unpublished", "authorIds": [999]}' http://localhost:8080/books
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "AUTHOR_NOT_FOUND"
   - エラーメッセージ: "著者ID 999 は存在しません"

6. 同じタイトル・著者の組み合わせの書籍が既に存在する場合
   > 注意: このテストを実行する前に、著者ID 1, 2が存在していることを確認してください。
   ```bash
   # 最初の書籍を登録
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": 1000, "status": "unpublished", "authorIds": [1, 2]}' http://localhost:8080/books

   # 同じタイトル・著者の組み合わせで登録（著者の順序が異なる場合も含む）
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": 2000, "status": "unpublished", "authorIds": [2, 1]}' http://localhost:8080/books
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "DUPLICATE_BOOK"
   - エラーメッセージ: "同じタイトル・著者の組み合わせの書籍が既に存在します"

## 4. 書籍更新（PUT /books）

### 正常系
1. 存在する書籍IDで、有効な更新内容が指定された場合
   > 注意: このテストを実行する前に、書籍ID 1と著者ID 1が存在していることを確認してください。
   ```bash
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "title": "更新されたタイトル", "price": 2000, "status": "published", "authorIds": [1]}' http://localhost:8080/books
   ```
   - 期待結果: 200 OK
   - レスポンス: 更新された書籍の情報

### 異常系
1. 存在しない書籍IDが指定された場合
   ```bash
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 999, "title": "更新テスト", "price": 2000, "status": "published", "authorIds": [1]}' http://localhost:8080/books
   ```
   - 期待結果: 404 Not Found
   - エラーコード: "BOOK_NOT_FOUND"
   - エラーメッセージ: "書籍ID 999 は存在しません"

2. 出版済みの書籍を未出版に変更しようとした場合
   > 注意: このテストを実行する前に、書籍ID 1が存在し、かつその書籍がpublished状態であることを確認してください。
   ```bash
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "title": "更新テスト", "price": 2000, "status": "unpublished", "authorIds": [1]}' http://localhost:8080/books
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "INVALID_STATUS"
   - エラーメッセージ: "出版済みの書籍を未出版に変更することはできません"

3. 存在しない著者IDが指定された場合
   ```bash
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "title": "更新テスト", "price": 2000, "status": "published", "authorIds": [999]}' http://localhost:8080/books
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "AUTHOR_NOT_FOUND"
   - エラーメッセージ: "著者ID 999 は存在しません"

4. タイトルが空の場合
   ```bash
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "title": "", "price": 2000, "status": "published", "authorIds": [1]}' http://localhost:8080/books
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "VALIDATION_ERROR"
   - エラーメッセージ: "タイトルは必須です"

5. 価格が負の数の場合
   ```bash
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "title": "更新テスト", "price": -2000, "status": "published", "authorIds": [1]}' http://localhost:8080/books
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "VALIDATION_ERROR"
   - エラーメッセージ: "価格は0以上である必要があります"

6. ステータスが不正な場合
   ```bash
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "title": "更新テスト", "price": 2000, "status": "invalid_status", "authorIds": [1]}' http://localhost:8080/books
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "VALIDATION_ERROR"
   - エラーメッセージ: "ステータスはunpublishedまたはpublishedである必要があります"

7. 著者IDが指定されていない場合
   ```bash
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "title": "更新テスト", "price": 2000, "status": "published", "authorIds": []}' http://localhost:8080/books
   ```
   - 期待結果: 400 Bad Request
   - エラーコード: "VALIDATION_ERROR"
   - エラーメッセージ: "著者は最低1人必要です"

## 5. 著者の書籍一覧取得（GET /authors/{authorId}/books）

### 正常系
1. 存在する著者IDが指定された場合
   ```bash
   curl http://localhost:8080/authors/1/books
   ```
   - 期待結果: 200 OK
   - レスポンス: 著者の書籍一覧

### 異常系
1. 存在しない著者IDが指定された場合
   ```bash
   curl http://localhost:8080/authors/999/books
   ```
   - 期待結果: 404 Not Found
   - エラーコード: "AUTHOR_NOT_FOUND"
   - エラーメッセージ: "著者ID 999 は存在しません"

## エラーレスポンス形式

すべてのエラーレスポンスは以下の形式で返されます：

```json
{
  "code": "エラーコード",
  "message": "エラーメッセージ"
}
```

主なエラーコード：
- `AUTHOR_NOT_FOUND`: 著者が存在しない場合
- `BOOK_NOT_FOUND`: 書籍が存在しない場合
- `INVALID_STATUS`: 書籍のステータス変更が無効な場合
- `VALIDATION_ERROR`: バリデーションエラーの場合
- `DUPLICATE_AUTHOR`: 同じ名前・生年月日の著者が既に存在する場合
- `DUPLICATE_BOOK`: 同じタイトルの書籍が既に存在する場合
- `SYSTEM_ERROR`: 予期せぬエラーが発生した場合

