# Frequently Asked Questions

### [← back](/)

### Question:

#### Where does VATprism get its data from?

### Answer:

Data is fetched from [the VATSIM API](https://api.vatsim.net/api/). Static data like FIR-Boundaries are updated on every
startup and are sourced from
the [VAT-Spy Client Data Update Project](https://github.com/vatsimnetwork/vatspy-data-project).

---

### Question:

#### Why does the text in VATprism look all scrambled?

[![Scrambled Text Issue](assets/images/faq/scrambled_text.png)](assets/images/faq/scrambled_text.png)

### Answer:

[This is most likely an issue with your Windows Fonts,](https://stackoverflow.com/a/66845136/3000387) more specifically
with a corrupted or missing Segoe UI font. To fix this issue, reinstall Segoe UI.

---

### Question:

#### Why is macOS telling me that VATprism is damaged and cannot be opened?

### Answer:

This is an issue with codesign and currently, there only exists a workaround that probably has to be executed after
every update.

1. Open Terminal
2. Navigate to the Applications folder with the command `cd /Applicationns/`
3. Run codesign with the command `codesign --remove-signature VATprism.app`

Hopefully, I will be able to resolve this in the future so that workaround becomes unnecessary.

### Question:

#### [VATprism scaling behaviour is broken on high DPI screens](https://github.com/marvk/vatprism/issues/47)

### Answer:

1. Locate `VATprism.exe` in `C:\Program Files\VATprism`
2. Right click -> Properties
3. Go to Compatibility tab
4. Check `Override high DPI scaling behavior`
5. Choose `System` for `Scaling performed by`

### [← back](/)
