#include <stdio.h>
#include <stdlib.h>
#include <winsock2.h>
#include <string.h>
#include <unistd.h>
#include <time.h>
#include <windows.h>

#pragma comment(lib, "ws2_32.lib") //telling the compiler to link with library

#define BUFFSIZE 1024
#define MAX_CLIENTS 10

int main(int argc, char *argv[]){

    WSADATA wsaData;
    unsigned int port;

    struct sockaddr_in address;
    char buffer[BUFFSIZE];

    int i, activity, recv_size, maxsd = 0, opt = 1, addrlen;
    SOCKET master_socket, client_socket[MAX_CLIENTS], new_socket;
    fd_set readfds;


    //random number generator from 0 to 9
    int rand_num;
    srand(time(NULL));
    rand_num = rand() % 10;
    char answer[BUFFSIZE];
    itoa(rand_num,answer,sizeof(buffer));

    // read port from args
    if (argc != 2){
        printf("USAGE: %s <port>\n", argv[0]);
        exit(EXIT_FAILURE);
    }

    port = atoi(argv[1]);

    if ((port < 1) || (port > 65535)){
        printf("ERROR #1: invalid port specified.\n");
        exit(EXIT_FAILURE);
    }

    // init WinSock
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0){
        printf("WSAStartup failed: %d\n", WSAGetLastError());
        exit(EXIT_FAILURE);
    }

    // create master socket (TCP)
    // it listens for incoming connection requests
    if ((master_socket = socket(AF_INET, SOCK_STREAM, 0)) == 0){
        printf("Failed to create socket: %d\n", WSAGetLastError());
        exit(EXIT_FAILURE);
    }

    if (setsockopt(master_socket, SOL_SOCKET, SO_REUSEADDR, (char *)&opt, sizeof(opt)) < 0){
        printf("Failed to set socket opt: %d\n", WSAGetLastError());
        exit(EXIT_FAILURE);
    }

    // set up the address
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(port);

    // bind the master socket to the address address
    if (bind(master_socket, (struct sockaddr *)&address, sizeof(address)) < 0){
        printf("Bind failed: %d\n", WSAGetLastError());
        exit(EXIT_FAILURE);
    }

    printf("Listening on port %d \n", port);

    // start listening for incoming connections, max 3
    if (listen(master_socket, 3) == SOCKET_ERROR){
        printf("Listen failed: %d\n", WSAGetLastError());
        exit(EXIT_FAILURE);
    }
    for (i = 0; i < MAX_CLIENTS; i++){
        client_socket[i] = -1;
    }


    addrlen = sizeof(address);
    puts("Waiting for connections...");
    while(1){

        // clear the socket set
        FD_ZERO(&readfds);
        FD_SET(master_socket, &readfds);
        maxsd = master_socket;
        // add client's socket to file descriptor set
        for (i = 0; i < MAX_CLIENTS; i++){
            if (client_socket[i] != -1){
                FD_SET(client_socket[i], &readfds);
            }
            if (client_socket[i] > maxsd){
                maxsd = client_socket[i];
            }
        }

        // wait for activity on any of the sockets
        activity = select(maxsd + 1, &readfds, NULL, NULL, NULL);

        if (activity < 0){
            printf("Select failed: %d\n", WSAGetLastError());
            exit(EXIT_FAILURE);
        }

        // accept new connection
        if (FD_ISSET(master_socket, &readfds)){
            if ((new_socket = accept(master_socket, (struct sockaddr *)&address, &addrlen)) < 0){
                printf("Accept failed: %d\n", WSAGetLastError());
                exit(EXIT_FAILURE);
            }

            printf("New connection from %s:%d\n", inet_ntoa(address.sin_addr), ntohs(address.sin_port));

            // add the new socket to the client socket array
            for (i = 0; i < MAX_CLIENTS; i++){
                // if position is empty
                if (client_socket[i] == -1){
                    client_socket[i] = new_socket;
                    break;
                }
            }
        }
        //handle activity on child sockets
        for(int i = 0; i < MAX_CLIENTS; i++){
            if(client_socket[i] != INVALID_SOCKET && FD_ISSET(client_socket[i], &readfds))
            {
                //receive smth from the client
                if((recv_size = recv(client_socket[i],buffer,sizeof(buffer), 0)) == SOCKET_ERROR){
                    printf("Failed to receive data from socket %d\n", client_socket[i]);
                    closesocket(client_socket[i]);
                    client_socket[i] = INVALID_SOCKET;
                } else if (recv_size == 0){
                    //Client disconnected
                    printf("Client disconnected: %s:%d\n",inet_ntoa(address.sin_addr), ntohs(address.sin_port));
                    client_socket[i] = INVALID_SOCKET;
                } else{
                    if(strncmp(answer,buffer,1) == 0){
                        send(client_socket[i],"You are right!", sizeof("You are right!"),0);

                        close(client_socket[i]);
                    }
                    else{

                        send(client_socket[i],"No, try again!", sizeof("No, try again!"),0);

                    }
                }
            }
        }
    }
}