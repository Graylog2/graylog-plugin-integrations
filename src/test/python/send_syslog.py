# This file is part of Graylog.
#
# Graylog is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Graylog is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Graylog.  If not, see <http://www.gnu.org/licenses/>.

import argparse
import socket
import time
import sys
from datetime import datetime


class SocketSender(object):
    def __init__(self, server: str = 'localhost', port: int = 514, use_udp: bool = False) -> None:
        self.socket = None
        self.server = server
        self.port = port
        self.clientname = socket.gethostname()
        self.protocol = socket.SOCK_STREAM
        if use_udp:
            self.protocol = socket.SOCK_DGRAM

    def connect(self) -> bool:
        if self.socket == None:
            address_info = socket.getaddrinfo(self.server, self.port, socket.AF_UNSPEC, self.protocol)
            if address_info == None:
                return False

            for (address_family, socket_type, protocol, canon_name, socket_address) in address_info:
                self.socket = socket.socket(address_family, self.protocol)
                if self.socket == None:
                    return False
                try:
                    self.socket.connect(socket_address)
                    return True
                except:
                    if self.socket != None:
                        self.socket.close()
                        self.socket = None
                    continue
            return False
        else:
            return True

    def close(self) -> None:
        if self.socket != None:
            self.socket.close()
            self.socket = None

    def send(self, message: str) -> None:
        syslog_message = "<14>1 %s PYTHON_TEST_SENDER - - - - %s\n" % (
            datetime.utcnow().isoformat() + 'Z',
            message
        )
        encoded = syslog_message.encode('utf-8')

        if self.socket != None or self.connect():
            try:
                self.socket.sendall(encoded)
            except:
                self.close()

class DelayValue(argparse.Action):
    def __call__(self, parser, namespace, values, options_string=None):
        if not 0 <= values <= 1000:
            raise argparse.ArgumentError(self, "Invalid delay setting, value must be between 0-1000")
        setattr(namespace, self.dest, values)


def send_single_message(client, message):
    client.send(message)
    client.close()


def send_messages_from_file(client, file, delay):
    input_file = open(file, 'r')
    # Track the number of lines
    replay_line_count = 0
    # Track the time to send
    timer_start = time.perf_counter()

    for line in input_file:
        client.send(line)
        time.sleep(delay * 0.001)
        replay_line_count += 1

    input_file.close()
    client.close()

    time_taken = round(time.perf_counter() - timer_start,4)

    print ( 'Replayed [%s] lines from [%s] in [%s] seconds' % ( replay_line_count, file, time_taken ), file=sys.stderr )
    

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description='A program for sending arbitrary syslog messages to a Graylog server using TCP. This can be used to test any Input built on Syslog TCP such as Palo Alto.',
        epilog='You must provide either a message or an input file.')
    parser.add_argument('-s', '--server', help='The syslog server (defaults to "localhost")', default='localhost',
                        type=str)
    parser.add_argument('-p', '--port', help='The syslog port (defaults to 514)', default=514, type=int)
    parser.add_argument('-u', '--udp', help='Send message using UDP rather than TCP', action='store_true')
    parser.add_argument('-d', '--delay', help='Add a delay in milliseconds [0-1000] after sending each line of a file', default=0, metavar="[0-1000]", type=int, action=DelayValue)
    group = parser.add_mutually_exclusive_group()
    group.add_argument('-m', '--message',
                       help="The message to be sent (may need to be surrounded by 'single quotes' if it contains spaces)",
                       type=str)
    group.add_argument('-f', '--file', help='A newline-delimited file of messages to be sent', type=str)
    args = parser.parse_args()

    client = SocketSender(args.server, args.port, args.udp)

    if args.message:
        send_single_message(client, args.message)
    elif args.file:
        send_messages_from_file(client, args.file, args.delay)
    else:
        parser.print_help()
