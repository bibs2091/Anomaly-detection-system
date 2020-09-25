import socket
from model import model

m = model()

def server_program():
    
    host = socket.gethostname()
    port = 5000 

    server_socket = socket.socket() 
    server_socket.bind((host, port)) 

    server_socket.listen(2)
    conn, address = server_socket.accept()  # accept new connection
    print("Connection from: " + str(address))
    while True:
        # won't accept data packet greater than 2048 bytes
        data = conn.recv(2048).decode()
        if not data:
            # if data is not received break
            break

        m.load_data(data)
        prediction = m.predict()
        #conn.send(str(prediction).encode())
    conn.close() 


if __name__ == '__main__':
    server_program()