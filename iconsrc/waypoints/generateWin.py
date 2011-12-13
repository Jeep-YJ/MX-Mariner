import os

template = '''\"C:\\Program Files (x86)\\Inkscape\\inkscape.exe\" --without-gui --export-png=\"..\\..\\res\\drawable\\%s.png\" --export-dpi=80 --export-background-opacity=0 --export-width=32 --export-height=32 %s.svg

\"C:\\Program Files (x86)\\Inkscape\\inkscape.exe\" --without-gui --export-png=\"..\\..\\res\\drawable\\%ssmall.png\" --export-dpi=40 --export-background-opacity=0 --export-width=16 --export-height=16 %s.svg

'''

fd = open("dopng.bat", 'w')

for eafile in os.listdir(os.getcwd()):
    if eafile.endswith(".svg"):
        ea = eafile.rstrip(".svg")
        fd.write( template %(ea,ea,ea,ea) )

fd.close()


