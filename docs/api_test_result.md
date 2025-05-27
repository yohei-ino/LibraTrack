# APIテスト結果

本ドキュメントは、著者・書籍リソースのバリデーションを含むAPIテストの実行結果をまとめたものです。

## 1. 著者登録（POST /authors）

### 正常系
1. 名前、生年月日が正しく設定されている場合
   ```bash
   # リクエスト
   curl -X POST -H "Content-Type: application/json" -d '{"name": "テスト著者", "birthDate": "1990-01-01"}' http://localhost:8080/authors
   ```
   ```json
   # レスポンス
   {
     "id": 1,
     "name": "テスト著者",
     "birthDate": "1990-01-01"
   }
   ```
   - 結果: 200 OK

### 異常系
1. 名前が空の場合
   ```bash
   # リクエスト
   curl -X POST -H "Content-Type: application/json" -d '{"name": "", "birthDate": "1990-01-01"}' http://localhost:8080/authors
   ```
   ```json
   # レスポンス
   {
     "code": "VALIDATION_ERROR",
     "message": "名前は必須です"
   }
   ```
   - 結果: 400 Bad Request

2. 生年月日が未来の日付の場合
   ```bash
   # リクエスト
   curl -X POST -H "Content-Type: application/json" -d '{"name": "未来の著者", "birthDate": "2030-01-01"}' http://localhost:8080/authors
   ```
   ```json
   # レスポンス
   {
     "code": "VALIDATION_ERROR",
     "message": "生年月日は過去の日付である必要があります"
   }
   ```
   - 結果: 400 Bad Request

3. 同じ名前・生年月日の著者が既に存在する場合
   ```bash
   # リクエスト
   curl -X POST -H "Content-Type: application/json" -d '{"name": "テスト著者", "birthDate": "1990-01-01"}' http://localhost:8080/authors
   ```
   ```json
   # レスポンス
   {
     "code": "DUPLICATE_AUTHOR",
     "message": "同じ名前・生年月日の著者が既に存在します"
   }
   ```
   - 結果: 400 Bad Request

## 2. 著者更新（PUT /authors）

### 正常系
1. 存在する著者IDで、有効な更新内容が指定された場合
   ```bash
   # リクエスト
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "name": "更新された著者", "birthDate": "1980-01-01"}' http://localhost:8080/authors
   ```
   ```json
   # レスポンス
   {
     "id": 1,
     "name": "更新された著者",
     "birthDate": "1980-01-01"
   }
   ```
   - 結果: 200 OK

### 異常系
1. 存在しない著者IDが指定された場合
   ```bash
   # リクエスト
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 999, "name": "更新テスト", "birthDate": "1980-01-01"}' http://localhost:8080/authors
   ```
   ```json
   # レスポンス
   {
     "code": "AUTHOR_NOT_FOUND",
     "message": "著者ID 999 は存在しません"
   }
   ```
   - 結果: 404 Not Found

2. 名前が空の場合
   ```bash
   # リクエスト
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "name": "", "birthDate": "1980-01-01"}' http://localhost:8080/authors
   ```
   ```json
   # レスポンス
   {
     "code": "VALIDATION_ERROR",
     "message": "名前は必須です"
   }
   ```
   - 結果: 400 Bad Request

3. 生年月日が未来の日付の場合
   ```bash
   # リクエスト
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "name": "更新テスト", "birthDate": "2030-01-01"}' http://localhost:8080/authors
   ```
   ```json
   # レスポンス
   {
     "code": "VALIDATION_ERROR",
     "message": "生年月日は過去の日付である必要があります"
   }
   ```
   - 結果: 400 Bad Request

## 3. 書籍登録（POST /books）

### 正常系
1. タイトル、価格、ステータス、著者IDが正しく設定されている場合
   ```bash
   # リクエスト
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": 1000, "status": "unpublished", "authorIds": [1]}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "id": 1,
     "title": "テスト本",
     "price": 1000.00,
     "status": "unpublished",
     "authors": [
       {
         "id": 1,
         "name": "更新された著者",
         "birthDate": "1980-01-01"
       }
     ]
   }
   ```
   - 結果: 200 OK

2. 同じタイトルでも著者の組み合わせが異なる場合は登録できること
   ```bash
   # リクエスト
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": 1000, "status": "unpublished", "authorIds": [1, 2]}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "id": 2,
     "title": "テスト本",
     "price": 1000.00,
     "status": "unpublished",
     "authors": [
       {
         "id": 1,
         "name": "更新された著者",
         "birthDate": "1980-01-01"
       },
       {
         "id": 2,
         "name": "テスト著者2",
         "birthDate": "1990-01-01"
       }
     ]
   }
   ```
   - 結果: 200 OK

### 異常系
1. タイトルが空の場合
   ```bash
   # リクエスト
   curl -X POST -H "Content-Type: application/json" -d '{"title": "", "price": 1000, "status": "unpublished", "authorIds": [1]}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "code": "VALIDATION_ERROR",
     "message": "タイトルは必須です"
   }
   ```
   - 結果: 400 Bad Request

2. 価格が負の数の場合
   ```bash
   # リクエスト
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": -1000, "status": "unpublished", "authorIds": [1]}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "code": "VALIDATION_ERROR",
     "message": "価格は0以上である必要があります"
   }
   ```
   - 結果: 400 Bad Request

3. ステータスが不正な場合
   ```bash
   # リクエスト
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": 1000, "status": "invalid_status", "authorIds": [1]}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "code": "VALIDATION_ERROR",
     "message": "ステータスはunpublishedまたはpublishedである必要があります"
   }
   ```
   - 結果: 400 Bad Request

4. 著者IDが指定されていない場合
   ```bash
   # リクエスト
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": 1000, "status": "unpublished", "authorIds": []}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "code": "VALIDATION_ERROR",
     "message": "著者は最低1人必要です"
   }
   ```
   - 結果: 400 Bad Request

5. 存在しない著者IDが指定された場合
   ```bash
   # リクエスト
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": 1000, "status": "unpublished", "authorIds": [999]}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "code": "AUTHOR_NOT_FOUND",
     "message": "著者ID 999 は存在しません"
   }
   ```
   - 結果: 400 Bad Request

6. 同じタイトル・著者の組み合わせの書籍が既に存在する場合
   ```bash
   # リクエスト
   curl -X POST -H "Content-Type: application/json" -d '{"title": "テスト本", "price": 2000, "status": "unpublished", "authorIds": [2, 1]}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "code": "DUPLICATE_BOOK",
     "message": "同じタイトル・著者の組み合わせの書籍が既に存在します"
   }
   ```
   - 結果: 400 Bad Request

## 4. 書籍更新（PUT /books）

### 正常系
1. 存在する書籍IDで、有効な更新内容が指定された場合
   ```bash
   # リクエスト
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "title": "更新されたタイトル", "price": 2000, "status": "published", "authorIds": [1]}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "id": 1,
     "title": "更新されたタイトル",
     "price": 2000.00,
     "status": "published",
     "authors": [
       {
         "id": 1,
         "name": "更新された著者",
         "birthDate": "1980-01-01"
       }
     ]
   }
   ```
   - 結果: 200 OK

### 異常系
1. 存在しない書籍IDが指定された場合
   ```bash
   # リクエスト
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 999, "title": "更新テスト", "price": 2000, "status": "published", "authorIds": [1]}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "code": "BOOK_NOT_FOUND",
     "message": "書籍ID 999 は存在しません"
   }
   ```
   - 結果: 404 Not Found

2. 出版済みの書籍を未出版に変更しようとした場合
   ```bash
   # リクエスト
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "title": "更新テスト", "price": 2000, "status": "unpublished", "authorIds": [1]}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "code": "INVALID_STATUS",
     "message": "出版済みの書籍を未出版に変更することはできません"
   }
   ```
   - 結果: 400 Bad Request

3. 存在しない著者IDが指定された場合
   ```bash
   # リクエスト
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "title": "更新テスト", "price": 2000, "status": "published", "authorIds": [999]}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "code": "AUTHOR_NOT_FOUND",
     "message": "著者ID 999 は存在しません"
   }
   ```
   - 結果: 400 Bad Request

4. タイトルが空の場合
   ```bash
   # リクエスト
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "title": "", "price": 2000, "status": "published", "authorIds": [1]}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "code": "VALIDATION_ERROR",
     "message": "タイトルは必須です"
   }
   ```
   - 結果: 400 Bad Request

5. 価格が負の数の場合
   ```bash
   # リクエスト
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "title": "更新テスト", "price": -2000, "status": "published", "authorIds": [1]}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "code": "VALIDATION_ERROR",
     "message": "価格は0以上である必要があります"
   }
   ```
   - 結果: 400 Bad Request

6. ステータスが不正な場合
   ```bash
   # リクエスト
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "title": "更新テスト", "price": 2000, "status": "invalid_status", "authorIds": [1]}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "code": "VALIDATION_ERROR",
     "message": "ステータスはunpublishedまたはpublishedである必要があります"
   }
   ```
   - 結果: 400 Bad Request

7. 著者IDが指定されていない場合
   ```bash
   # リクエスト
   curl -X PUT -H "Content-Type: application/json" -d '{"id": 1, "title": "更新テスト", "price": 2000, "status": "published", "authorIds": []}' http://localhost:8080/books
   ```
   ```json
   # レスポンス
   {
     "code": "VALIDATION_ERROR",
     "message": "著者は最低1人必要です"
   }
   ```
   - 結果: 400 Bad Request

## 5. 著者の書籍一覧取得（GET /authors/{authorId}/books）

### 正常系
1. 存在する著者IDが指定された場合
   ```bash
   # リクエスト
   curl http://localhost:8080/authors/1/books
   ```
   ```json
   # レスポンス
   [
     {
       "id": 2,
       "title": "テスト本",
       "price": 1000.00,
       "status": "unpublished",
       "authors": [
         {
           "id": 1,
           "name": "更新された著者",
           "birthDate": "1980-01-01"
         }
       ]
     },
     {
       "id": 1,
       "title": "更新されたタイトル",
       "price": 2000.00,
       "status": "published",
       "authors": [
         {
           "id": 1,
           "name": "更新された著者",
           "birthDate": "1980-01-01"
         }
       ]
     }
   ]
   ```
   - 結果: 200 OK

### 異常系
1. 存在しない著者IDが指定された場合
   ```bash
   # リクエスト
   curl http://localhost:8080/authors/999/books
   ```
   ```json
   # レスポンス
   {
     "code": "AUTHOR_NOT_FOUND",
     "message": "著者ID 999 は存在しません"
   }
   ```
   - 結果: 404 Not Found 