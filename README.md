# 射抜け！スナイパーの森

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.3-brightgreen)
![Paper API](https://img.shields.io/badge/Paper--API-1.21.3-blue)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

射抜け！スナイパーの森"は、プレイヤー同士の戦闘をより激しく、または面白くするMinecraftプラグインです。

## 概要 📝

このプラグインはミニゲーム用のプラグインです。

## 特徴 ✨

- [ ] ワンショット機能：頭上に表示されたアイテムを射抜くことでプレイヤーを一撃で倒すことが可能
- [ ] ランダム化システム：アイテムのターゲットをランダム化する機能
- [ ] バフシステム：カスタマイズ可能なバフ効果

## コマンド 🎮

基本コマンド: `/osg` または `/one_shot_glory`

サブコマンド:
- `/osg help` - ヘルプメニューを表示
- `/osg reload` - 設定をリロード
- `/osg config` - 設定を管理
- `/osg start` - プラグインを開始
- `/osg stop` - プラグインを停止

## 権限 🔒

- `one_shot_glory.command` - 基本コマンドの使用権限（デフォルト：全員）
- `osg.config` - 設定管理の権限（デフォルト：OP）
- `osg.reload` - リロードコマンドの権限（デフォルト：OP）
- `osg.start` - 開始コマンドの権限（デフォルト：OP）
- `osg.stop` - 停止コマンドの権限（デフォルト：OP）

## 設定 ⚙️

`config.yml` で以下の設定が可能です：

```yaml
# ターゲットアイテムのランダム化を有効にする
randomize_targets_items: true

# アイテム入手時のバフ効果を有効にする
enable_buffs: true

# リスポーン時の体力設定遅延（ティック）
respawn_set_health_delay: 15
```

## 要件 📋

- Minecraft 1.21.3
- Paper サーバー
- Java 21

## 作者 👤

企画: Hashibutogarasu

開発者: Hashibutogarasu

## ライセンス 📄

このプロジェクトは [MIT License](./LICENCE.md) の下で公開されています。