一、发送和接受Json：
确定本题目的客户端和服务器间通过Json数据交换互相通信。
下面是一个使用 Java-WebSocket 库（一个轻量级的 Java WebSocket 客户端/服务端实现），提供一个仅收发 JSON 消息的示例，不包含任何揭棋逻辑。同学们也可以选择其它方式收发Json数据。
1. 创建 Maven 项目（或直接添加依赖）
pom.xml
xml
<dependencies>
    <dependency>
        <groupId>org.java-websocket</groupId>
        <artifactId>Java-WebSocket</artifactId>
        <version>1.5.7</version>
    </dependency>
    <!-- 用于处理 JSON -->
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>
</dependencies>
如果不使用 Maven，请手动下载 Java-WebSocket-1.5.7.jar 和 gson-2.10.1.jar 并添加到 classpath。
2. 服务端代码
JsonWebSocketServer.java
java
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
public class JsonWebSocketServer extends WebSocketServer {
    public JsonWebSocketServer(InetSocketAddress address) {
        super(address);
    }
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("新连接: " + conn.getRemoteSocketAddress());
    }
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("连接关闭: " + conn.getRemoteSocketAddress());
    }
    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("收到消息: " + message);
        // 解析 JSON
        JsonObject json = JsonParser.parseString(message).getAsJsonObject();
        String msgType = json.get("messageType").getAsString();
        // 构造响应 JSON (仅演示回显)
        JsonObject response = new JsonObject();
        response.addProperty("messageType", "echo");
        response.addProperty("originalType", msgType);
        response.addProperty("timestamp", System.currentTimeMillis());
        // 发送响应
        conn.send(response.toString());
    }
    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }
    @Override
    public void onStart() {
        System.out.println("WebSocket 服务端已启动，端口: " + getPort());
    }
    public static void main(String[] args) {
        int port = 8887;
        JsonWebSocketServer server = new JsonWebSocketServer(new InetSocketAddress(port));
        server.start();
        System.out.println("服务端运行在 ws://localhost:" + port);
    }
}
3. 客户端代码
JsonWebSocketClient.java
java
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
public class JsonWebSocketClient extends WebSocketClient {
    public JsonWebSocketClient(URI serverUri) {
        super(serverUri);
    }
    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("已连接到服务器");
        // 发送一条 JSON 消息示例
        JsonObject msg = new JsonObject();
        msg.addProperty("messageType", "hello");
        msg.addProperty("content", "Hello, Server!");
        String jsonMsg = msg.toString();
        send(jsonMsg);
        System.out.println("发送: " + jsonMsg);
    }
    @Override
    public void onMessage(String message) {
        System.out.println("收到服务器消息: " + message);
        // 解析并处理 JSON 响应
        JsonObject resp = JsonParser.parseString(message).getAsJsonObject();
        String msgType = resp.get("messageType").getAsString();
        if ("echo".equals(msgType)) {
            System.out.println("服务器回显类型: " + resp.get("originalType").getAsString());
        }
    }
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("连接关闭: " + reason);
    }
    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
    public static void main(String[] args) throws Exception {
        URI uri = new URI("ws://localhost:8887");
        JsonWebSocketClient client = new JsonWebSocketClient(uri);
        client.connect();
        // 等待几秒后关闭（实际可保持连接）
        Thread.sleep(5000);
        client.close();
    }
}
4. 运行与测试
先启动服务端：运行 JsonWebSocketServer.main()
输出：
服务端运行在 ws://localhost:8887
WebSocket 服务端已启动，端口: 8887
再启动客户端：运行 JsonWebSocketClient.main()
输出：
已连接到服务器
发送: {"messageType":"hello","content":"Hello, Server!"}
服务端控制台输出：
输出：
新连接: /127.0.0.1:xxxxx
收到消息: {"messageType":"hello","content":"Hello, Server!"}
客户端收到响应：
收到服务器消息: {"messageType":"echo","originalType":"hello","timestamp":1712345678901}
服务器回显类型: hello
连接关闭: 正常断开
5. 核心要点
Java-WebSocket 库提供了纯 Java 的 WebSocket 实现，无需 Servlet 容器。
Gson 用于解析和生成 JSON 字符串，简单高效。
服务端在 onMessage 中收到 JSON，解析后可以按 messageType 做不同处理（这里只做回显）。
客户端连接成功后立即发送一个 JSON，然后在 onMessage 中处理响应。
你可以随意修改发送的 JSON 内容（例如增加字段），完全自由，不涉及任何揭棋逻辑。如果需要双向多次发送，只需在客户端或服务端中再次调用 send() 即可。
二、揭棋通信协议：
下面给出揭棋客户端与服务器之间的 完整 JSON 消息格式，涵盖游戏生命周期中的所有交互，包括：
账号与匹配
先手协商
对局进行（移动、翻子）
超时与心跳（10s通信一次）
游戏结束
每个消息都包含 messageType 字段，其他字段根据类型不同而不同。所有取值均枚举列出。
1. 客户端 → 服务器
messageType
方向
说明
字段
Login
C→S
登录
userId, password
register
C→S
注册
userId, password, nickname
startMatch
C→S
开始匹配
无额外字段
cancelMatch
C→S
取消匹配
无额外字段
requestFirstHand（如果10s内双方都不选择服务器随机设置先后手）
C→S
请求先手（红方）
wannaFirst: true/false
move
C→S
走子（第一步带有翻子）
fromX, fromY, toX, toY, isFlip: true/false
ping
C→S
心跳
timestamp: 长整型（毫秒）
Resign
C→S
认输
无额外字段
Ready
C→S
准备就绪（房间内）
无额外字段
2. 服务器 → 客户端
messageType
方向
说明
字段
loginResult
S→C
登录结果
success: true/falsemessage: 字符串（失败原因）userId（成功时）
matchSuccess
S→C
匹配成功
roomId: 字符串opponentId: 字符串opponentNickname: 字符串
gameStart
S→C
游戏开始，告知身份与先手
redPlayerId: 字符串（红方，先手）blackPlayerId: 字符串（黑方）yourColor: "red"/"black"firstHand: true/false（本方是否先手）initialBoard: 棋盘数组（见下文）
moveResult
S→C
走子结果（valid为true发送到双方客户端,否则只发送到发送move消息的客户端）
success: true/falsemove: 同客户端的 move 对象flipResult: 若 isFlip 为真，增加该字段，值为翻出的棋子类型（见棋子类型枚举）valid:判断客户端走子是否正确。true/false
timeout
S→C
超时判负（发送到2个客户端）
loserId: 字符串winnerId: 字符串reason: "timeout"
gameOver
S→C
游戏正常结束（广播）
winner: "red"/"black"reason: "checkmate"/"resign"winnerId: 字符串
pong
S→C
心跳回复
timestamp: 长整型（原样返回）
error
S→C
错误信息
code: 整数message: 字符串
roomInfo
S→C
房间状态更新（如对手准备）
opponentReady: true/false
默认每步1分钟，协商超时时间设置也作为可选内容，有兴趣的同学可自行定义。
选择可选消息的服务器或者客户端应该主动兼容没有可选内容的客户端和服务器。
3. 公共数据结构
3.1 棋盘表示（initialBoard 或 moveResult 中的可选状态）
棋盘为 9x10 网格，每个格子用以下格式表示：
json
{
  "x": a-i之一, "y": 0-9之一,
  "piece": "rook",   // 棋子类型，见下方枚举
  "visible": false     // false=暗棋，true=明棋
}
棋子类型枚举（piece 取值）：
红方
说明
Rook
车
Knight
马
Cannon
炮
Bishop
相（象）
Guard
士（仕）
King
帅（将）
Pawn
兵（卒）
暗棋状态：初始所有 visible 为 false。走第一步时 isFlip 为 true，翻开的棋子类型由服务器决定并修改piece，然后visible为true。同时把相关信息发送给双方客户端。
4. 完整通信流程示例
4.1 匹配与先手协商
json
// C→S 开始匹配
{ "messageType": "startMatch" }
// S→C 匹配成功
{
  "messageType": "matchSuccess",
  "roomId": "room_123",
  "opponentId": "user456",
  "opponentNickname": "象棋高手"
}
// C→S 请求先手
{ "messageType": "requestFirstHand", "wannaFirst": true }
// S→C 游戏开始
{
  "messageType": "gameStart",
  "redPlayerId": "user123",
  "blackPlayerId": "user456",
  "yourColor": "red",
  "firstHand": true,
  "initialBoard": [
    { "x": a, "y": 0, "piece": "rook", "visible": false },
    ...
  ]
}
4.2 走子与翻子
json
// C→S 移动（翻子）
{
  "messageType": "move",
  "fromX": a, "fromY": 0,
  "toX": a, "toY": 1,
  "isFlip": true
}
// S→C 移动结果（广播）
{
  "messageType": "moveResult",
  "success": true,
  "move": { "fromX":a, "fromY":0, "toX":a, "toY":2, "isFlip":true },
  "flipResult": "cannon",
  “valid”: true
}
4.3 超时处理
json
// S→C 超时（广播）
{
  "messageType": "timeout",
  "loserId": "user123",
  "winnerId": "user456",
  "reason": "timeout"
}
4.4 正常结束
json
// S→C 将死对方
{
  "messageType": "gameOver",
  "winner": "red",
  "reason": "checkmate",
  "winnerId": "user123"
}
4.5 心跳
json
// C→S
{ "messageType": "ping", "timestamp": 1712345678901 }
// S→C
{ "messageType": "pong", "timestamp": 1712345678901 }
5. 错误码枚举（error.code）
code
含义
1001
登录失败（账号或密码错误）
1002
重复登录
2001
非法走子（规则不符）
2002
未轮到本方走子
2003
超时未走子
3001
房间不存在
3002
匹配失败（无对手）
4001
JSON 格式错误
以上格式覆盖了揭棋客户端与服务器之间所有必要的 JSON 交换，并且每个消息属性的取值范围均已详细列出。可根据实际开发语言（如 Java）直接使用这些结构进行序列化/反序列化。