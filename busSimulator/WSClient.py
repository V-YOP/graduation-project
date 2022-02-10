
import websocket
import threading
import time
class WebsocketClient(object):
    """docstring for WebsocketClient"""
    def __init__(self, address, message_callback = None):
        super(WebsocketClient, self).__init__()
        self.address = address
        self.message_callback = message_callback

    def on_message(self, ws, message):
        # message = json.loads(message)
        print("on_client_message:", message)
        if self.message_callback:
            self.message_callback(message)

    def on_error(self, ws, error):
        print("client error:",error)

    def on_close(self, ws):
        print("### client closed ###")
        self.ws.close()
        self.is_running = False

    def on_open(self, ws):
        self.is_running = True
        print("on open")

    def close_connect(self):
        self.ws.close()
    
    def send_message(self, message):
        try:
            self.ws.send(message)
        except BaseException as err:
            pass
        

    def run(self):
        # websocket.enableTrace(True)
        self.ws = websocket.WebSocketApp(self.address,
                              on_message = lambda ws,message: self.on_message(ws, message),
                              on_error = lambda ws, error: self.on_error(ws, error),
                              on_close = lambda ws :self.on_close(ws))
        self.ws.on_open = lambda ws: self.on_open(ws)
        self.is_running = False
        while True:
            print(self.is_running)
            if not self.is_running:
                self.ws.run_forever()
            time.sleep(3)


class WSClient(object):
    def __init__(self, address, fn): # 将间隔执行fn并发送其返回值到后端
        super(WSClient, self).__init__()
        self.client = WebsocketClient(address, None)
        self.client_thread = None
        self.fn = fn
        self.interval = 1;
        self.interval_thread = None

    # 初始化后只需调用该方法
    def run(self):
        self.client_thread = threading.Thread(target=self.run_client)
        def ffn():
            while(True):
                data = self.fn()
                self.send_message(data)
                time.sleep(self.interval)
        self.interval_thread = threading.Thread(target=ffn)
        self.client_thread.start()
        self.interval_thread.start()
    def run_client(self):
        self.client.run()
        

    def send_message(self, message):
        self.client.send_message(message)

