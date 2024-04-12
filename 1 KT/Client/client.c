#include <winsock2.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#define BUFFSIZE 1024

int main(int argc, char *argv[]){

    WSADATA wsaData;
    unsigned int port;
    int sock;
    struct sockaddr_in address; //serverio adreso struktura

    char buffer[BUFFSIZE];
    char answer[] = "Y";


    if (argc != 3)
    {
        fprintf(stderr, "USAGE: %s <ip> <port>\n", argv[0]);
        exit(1);
    }

    port = atoi(argv[2]);

    if ((port < 1) || (port > 65535))
    {
        printf("ERROR #1: invalid port specified.\n");
        exit(1);
    }

    // init WinSock
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0)
    {
        printf("WSAStartup failed: %d\n", WSAGetLastError());
        return 1;
    }

    //socket creation

    if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0)
    {
        printf("Failed to create socket: %d\n", WSAGetLastError());
        return 1;
    }

    // set up the address and port
    memset(&address, 0, sizeof(address));
    address.sin_family = AF_INET;   // nurodomas protokolas (IP)
    address.sin_port = htons(port); // nurodomas portas
    address.sin_addr.s_addr = inet_addr(argv[1]);


    //********prisijungimas prie serverio*******\\

    if (connect(sock, (struct sockaddr *)&address, sizeof(address)) < 0)
    {
        fprintf(stderr, "ERROR #4: error in connect().\n");
        exit(1);
    }

    printf("Welcome to number guesser! Guess the number between 0-9!\n");

    while(1)
    {
        fgets(buffer,sizeof(buffer), stdin);
        send(sock,buffer,sizeof(buffer), 0); //spejimo issiuntimas
        memset(&buffer,0,BUFFSIZE);
        recv(sock,buffer,sizeof(buffer),0); //atsakymas
        printf("Server response: %s\n", buffer);

        if(strncmp(buffer,answer,1) == 0){
            closesocket(sock);
            break;
        }

    }
    WSACleanup();
    return 0;
}