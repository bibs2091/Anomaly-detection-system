import netifaces
interface = netifaces.gateways()['default'][netifaces.AF_INET][1]
print(interface)
#details about the interface
#print(netifaces.ifaddresses(interface))