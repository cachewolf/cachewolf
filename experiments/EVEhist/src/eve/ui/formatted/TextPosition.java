package eve.ui.formatted;
import eve.fx.DisplayLine;
import eve.sys.Device;
import eve.ui.TextPad;

	//##################################################################
	class TextPosition{
	//##################################################################
	// Do not move these next three -------------------
	DisplayLine line;
	int lineIndex;
	int characterIndex;
	//-------------------------------------------------
	//===================================================================
	public TextPosition(){}
	//===================================================================
	public TextPosition(TextPad pad)
	//===================================================================
	{
		line = pad.getLine(0);
		lineIndex = characterIndex = 0;
	}
	//===================================================================
	public TextPosition(TextPad pad,int lineIndex,int charIndex)
	//===================================================================
	{
		line = pad.getLine(lineIndex);
		this.lineIndex = lineIndex;
		this.characterIndex = charIndex;
	}
	//===================================================================
	TextPosition getCopy()
	//===================================================================
	{
		TextPosition tp = new TextPosition();
		tp.copyFrom(this);
		return tp;
	}
	//===================================================================
	void copyFrom(TextPosition tp)
	//===================================================================
	{
		line = tp.line;
		lineIndex = tp.lineIndex;
		characterIndex = tp.characterIndex;
	}
	native boolean nativeFindCharacter(int indexOfCharacter);
	boolean hasNative = true;	
	//===================================================================
	boolean findCharacter(int indexOfCharacter)
	//===================================================================
	{
		if (hasNative) try{
			return nativeFindCharacter(indexOfCharacter);
		}catch(Throwable t){
			Device.checkNoNativeMethod(t);
			hasNative = false;
		}
		if (characterIndex > indexOfCharacter) return false;
		while(true){
			int tl = line.trueLength();
			if (characterIndex+tl > indexOfCharacter && tl != 0)
				return true;
			characterIndex += tl;
			lineIndex++;
			if (line.next == null) return false;
			line = (DisplayLine)line.next;
		}
	}
	//===================================================================
	boolean moveToNextLine()
	//===================================================================
	{
		lineIndex++;
		characterIndex += line.trueLength();
		line = (DisplayLine)line.next;
		return line != null;
	}
	//##################################################################
	}
	//##################################################################

