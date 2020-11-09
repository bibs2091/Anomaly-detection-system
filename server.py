#!/usr/bin/env python
import socket
from model import model

m = model()

#check for unwanted bytes and remove them
def check_flow_return_string(recv):
    allowed_chars = ["0","1","2","3","4","5","6","7","8","9",'.',',','-','N','e','d','M','a','n','u','l','L','b','E','p','o','i','t']
    data =  ""
    for i in recv:
        char = chr(i)
        if char in allowed_chars:                
            data += chr(i)
    return data


def server_program():
    
    host = "localhost"
    port = 5000 

    server_socket = socket.socket() 
    server_socket.bind((host, port)) 

    server_socket.listen(2)
    conn, address = server_socket.accept()  # accept new connection
    data = [] #list of recieved data
    while True:
        # won't accept data packet greater than 2048 bytes

        recv = conn.recv(2048)
        
        data_temp = check_flow_return_string(recv)
        if not data_temp:
            # if data_temp is not received break
            break
        data.append(data_temp) #append the recieved data to data list

        #check if we recived x number of data
        if (len(data) == 10): 
            m.load_data(''.join(data)) #concat into one string
            m.predict()
            data = [] #clear list
        
        #conn.send(str(prediction).encode())
    conn.close() 


if __name__ == '__main__':
    server_program()