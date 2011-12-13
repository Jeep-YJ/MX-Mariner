import os

template = '''inkscape --without-gui --export-png=\"../../res/drawable-hdpi/%s.png\" --export-dpi=202.50 --export-background-opacity=0 --export-width=72 --export-height=72 %s.svg

inkscape --without-gui --export-png=\"../../res/drawable-ldpi/%s.png\" --export-dpi=101.25 --export-background-opacity=0 --export-width=36 --export-height=36 %s.svg

inkscape --without-gui --export-png=\"../../res/drawable-mdpi/%s.png\" --export-dpi=135 --export-background-opacity=0 --export-width=48 --export-height=48 %s.svg

'''

fd = open("dopng.sh", 'w')

for eafile in os.listdir(os.getcwd()):
    if eafile.endswith(".svg"):
        ea = eafile.rstrip(".svg")
        fd.write( template %(ea,ea,ea,ea,ea,ea) )

fd.close()


