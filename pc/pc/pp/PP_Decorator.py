class PP_Decorator( object ):

    def linefeed( self ):
        """Linefeed character"""
        return "\n"
    
    def indent( self, cols ):
        """Indentation string"""
        return " " * cols

    def escape( self, s ):
        """Escape characters in s"""
        return s

    def decorate( self, s, t ):
        """Return decorated s (s is escaped prior to calling this method!)"""
        return s



class Latex_PP_Decorator( PP_Decorator ):

    # List of replacements. Take care of order!
    ESCAPE_MAP = [ ("\\", "\\\\"),
                   ("{", "\\{"),
                   ("}", "\\}"),
                   ("_", "\_"), 
                   ("|", "$|$"), 
                   ("[", "$[$"), 
                   ("]", "$]$"), 
                   ]

    REPLACE_MAP = { "KEY_AND": " $\\wedge$ ",
                    "KEY_OR": " $\\vee$ ",
                    "KEY_IMPLIES": " $\\Rightarrow$ ",
                    "KEY_FORALL": "$\\forall$",
                    "KEY_EXISTS": "$\\exists$",
                    "GT": "$>$",
                    "GT_EQUAL": "$\\ge$",
                    "LT": "$<$",
                    "LT_EQUAL": "$\\le$",
                    "SLASH_EQUAL": "$\\ne$",
                    }



    def linefeed( self ):
        return "\\mbox{}\\\\ \n"
        

    def indent( self, cols ):
        if cols:
            return "\hspace*{%dex}"%cols
        else:
            return ""


    def escape( self, s ):
        for r,m in self.ESCAPE_MAP:
            s = s.replace(r,m)
        return s


    def decorate( self, s, t ):
        s = self.REPLACE_MAP.get(t,s)
        if t.startswith("KEY_"):
            s = "\\textbf{%s}"%s
        if s.upper()=="OLD":
            s = "\\textbf{%s}"%s.lower()
        return s

