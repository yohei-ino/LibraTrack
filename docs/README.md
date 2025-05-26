# LibraTrack

書籍管理システム `LibraTrack` の環境セットアップ及びその使用方法に関するドキュメントです。

## 使用方法

### DBコンテナの起動

```bash
podman compose up -d
```

### マイグレーション

```bash
./gradlew flywayMigrate
```

### アプリケーションの起動

別タブなどで別のターミナルを起動して、プロジェクトのルートディレクトリにて以下コマンドを実行してください。

```bash
./gradlew bootRun
```

## 確認用コマンド

### DBの確認

#### データベースへの接続

データベースの中身を確認したい場合には、以下コマンドを実行してください。

```bash
podman exec postgres psql -U postgres -d libratrack -c "SELECT * FROM authors;"
podman exec postgres psql -U postgres -d libratrack -c "SELECT * FROM books;"
podman exec postgres psql -U postgres -d libratrack -c "SELECT * FROM book_authors;"
```

DBコンテナの中から直接確認したい場合には、以下コマンドを実行して、データベースへ接続してください。

```bash
podman exec -it postgres /bin/bash
psql -U postgres -d libratrack
```

### APIリクエスト

[APIテスト手順](./api_test_guide.md)を参照してください。


## 停止

### アプリケーションの停止

`./gradlew bootRun` を実行したターミナルにて `Ctrl+C`を入力して、アプリケーションを停止してください。

### コンテナの停止

```bash
podman compose down
```

ボリュームごと削除する場合には`-v`をつけてください。

```bash
podman compose down -v
```


