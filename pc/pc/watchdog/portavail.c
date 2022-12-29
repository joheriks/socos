#include <stdio.h>
#include <stdlib.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

int main(int argc, char* argv[]);

int main(int argc, char* argv[])
{
   int desc;
   struct sockaddr_in sock;
   int portnr;

   if(argc!=2)
   {
      printf("syntax: portavail [port number]\n");
      return 1;
   }

   portnr=atoi(argv[1]);

   if(portnr==0)
   {
      printf("invalid port number\n");
      return 1;
   }   

   desc=socket(AF_INET, SOCK_STREAM, 0);

   if(desc<0)
   {
      printf("socket() failed\n");
      return 1;
   }

   sock.sin_family=AF_INET;
   sock.sin_addr.s_addr=htonl(INADDR_ANY);
   sock.sin_port=htons(portnr);

   if(bind(desc, (struct sockaddr*)&sock, sizeof(sock))<0)
   {
      printf("bind() failed. port %d is not available\n", portnr);
      return 1;
   }

   printf("port %d is available\n", portnr);

   return 0;
}
