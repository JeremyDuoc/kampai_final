import os
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

def create_document():
    doc = Document()

    # Configuración de márgenes estándar (2.5 cm / 1 pulgada)
    sections = doc.sections
    for section in sections:
        section.top_margin = Inches(1.0)
        section.bottom_margin = Inches(1.0)
        section.left_margin = Inches(1.0)
        section.right_margin = Inches(1.0)
        section.page_width = Inches(8.5)
        section.page_height = Inches(11.0)

    # Colores de la paleta Kampai
    COLOR_PRIMARY_VIOLET = RGBColor(106, 27, 154)   # #6A1B9A
    COLOR_SECONDARY_PINK = RGBColor(225, 0, 255)    # #E100FF
    COLOR_DARK_TEXT      = RGBColor(30, 30, 40)      # #1E1E28
    COLOR_MUTED_TEXT     = RGBColor(100, 116, 139)   # #64748B
    COLOR_AMBER          = RGBColor(217, 119, 6)     # #D97706
    COLOR_CYAN           = RGBColor(6, 182, 212)     # #06B6D4

    HEX_PRIMARY_VIOLET   = "6A1B9A"
    HEX_SECONDARY_PINK   = "E100FF"
    HEX_AMBER            = "D97706"
    HEX_CYAN             = "06B6D4"
    HEX_LIGHT_VIOLET_BG  = "F3E8FF"
    HEX_LIGHT_AMBER_BG   = "FEF3C7"
    HEX_LIGHT_CYAN_BG    = "ECFEFF"
    HEX_LIGHT_GRAY_BG    = "F8FAFC"
    HEX_DARK_BG          = "121212"
    HEX_BORDER_GRAY      = "CBD5E1"

    # Helpers XML
    def set_cell_background(cell, hex_color):
        tcPr = cell._tc.get_or_add_tcPr()
        tcPr.append(parse_xml(f'<w:shd {nsdecls("w")} w:fill="{hex_color}"/>'))

    def set_cell_margins(cell, top=140, bottom=140, left=180, right=180):
        tcPr = cell._tc.get_or_add_tcPr()
        tcMar = OxmlElement('w:tcMar')
        for m, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
            node = OxmlElement(f'w:{m}')
            node.set(qn('w:w'), str(val))
            node.set(qn('w:type'), 'dxa')
            tcMar.append(node)
        tcPr.append(tcMar)

    def set_cell_border(cell, top=None, bottom=None, left=None, right=None):
        tcPr = cell._tc.get_or_add_tcPr()
        tcBorders = OxmlElement('w:tcBorders')
        borders = {'top': top, 'bottom': bottom, 'left': left, 'right': right}
        for b_name, b_val in borders.items():
            if b_val:
                node = parse_xml(f'<w:{b_name} {nsdecls("w")} w:val="{b_val.get("val","single")}" w:sz="{b_val.get("sz","4")}" w:space="0" w:color="{b_val.get("color","CBD5E1")}"/>')
                tcBorders.append(node)
            else:
                node = parse_xml(f'<w:{b_name} {nsdecls("w")} w:val="none"/>')
                tcBorders.append(node)
        tcPr.append(tcBorders)

    def add_callout(doc, title, text, border_color="6A1B9A", bg_color="F3E8FF", icon="💡"):
        tbl = doc.add_table(rows=1, cols=1)
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        tbl.autofit = False
        cell = tbl.cell(0, 0)
        cell.width = Inches(6.5)
        set_cell_background(cell, bg_color)
        set_cell_margins(cell, top=120, bottom=120, left=180, right=180)
        set_cell_border(cell, left={"val": "single", "sz": "32", "color": border_color})
        
        p = cell.paragraphs[0]
        p.paragraph_format.space_before = Pt(2)
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.line_spacing = 1.15
        
        run_title = p.add_run(f"{icon}  {title}\n")
        run_title.bold = True
        run_title.font.name = "Segoe UI"
        run_title.font.size = Pt(10.5)
        run_title.font.color.rgb = RGBColor.from_string(border_color)
        
        run_text = p.add_run(text)
        run_text.font.name = "Segoe UI"
        run_text.font.size = Pt(9.5)
        run_text.font.color.rgb = COLOR_DARK_TEXT
        
        doc.add_paragraph().paragraph_format.space_after = Pt(4)

    def format_heading(doc, text, level):
        h = doc.add_heading(text, level=level)
        h.paragraph_format.keep_with_next = True
        run = h.runs[0]
        run.font.name = "Segoe UI"
        if level == 1:
            run.font.size = Pt(17)
            run.bold = True
            run.font.color.rgb = COLOR_PRIMARY_VIOLET
            h.paragraph_format.space_before = Pt(18)
            h.paragraph_format.space_after = Pt(8)
            
            # Línea divisoria elegante bajo Heading 1
            p_line = doc.add_paragraph()
            p_line.paragraph_format.space_before = Pt(0)
            p_line.paragraph_format.space_after = Pt(8)
            p_line_border = parse_xml(f'<w:pBdr {nsdecls("w")}><w:bottom w:val="single" w:sz="14" w:space="1" w:color="{HEX_PRIMARY_VIOLET}"/></w:pBdr>')
            p_line._p.get_or_add_pPr().append(p_line_border)
        elif level == 2:
            run.font.size = Pt(13)
            run.bold = True
            run.font.color.rgb = COLOR_PRIMARY_VIOLET
            h.paragraph_format.space_before = Pt(14)
            h.paragraph_format.space_after = Pt(6)
        elif level == 3:
            run.font.size = Pt(11)
            run.bold = True
            run.font.color.rgb = COLOR_AMBER
            h.paragraph_format.space_before = Pt(10)
            h.paragraph_format.space_after = Pt(4)
        return h

    def add_p(doc, text="", bold_prefix="", space_after=6, italic=False):
        p = doc.add_paragraph()
        p.paragraph_format.space_after = Pt(space_after)
        p.paragraph_format.line_spacing = 1.15
        if bold_prefix:
            r_bold = p.add_run(bold_prefix)
            r_bold.bold = True
            r_bold.font.name = "Segoe UI"
            r_bold.font.size = Pt(10)
            r_bold.font.color.rgb = COLOR_DARK_TEXT
        if text:
            r = p.add_run(text)
            r.font.name = "Segoe UI"
            r.font.size = Pt(10)
            r.italic = italic
            r.font.color.rgb = COLOR_DARK_TEXT
        return p

    def add_bullet(doc, title, desc):
        p = doc.add_paragraph(style='List Bullet')
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.line_spacing = 1.15
        r_t = p.add_run(title + ": ")
        r_t.bold = True
        r_t.font.name = "Segoe UI"
        r_t.font.size = Pt(10)
        r_t.font.color.rgb = COLOR_PRIMARY_VIOLET
        
        r_d = p.add_run(desc)
        r_d.font.name = "Segoe UI"
        r_d.font.size = Pt(10)
        r_d.font.color.rgb = COLOR_DARK_TEXT

    def style_table(table, col_widths, headers, data):
        table.alignment = WD_TABLE_ALIGNMENT.CENTER
        table.autofit = False
        
        # Header Row
        hdr_row = table.rows[0]
        hdr_tr = hdr_row._tr.get_or_add_trPr()
        hdr_tr.append(parse_xml(f'<w:tblHeader {nsdecls("w")}/>'))
        
        for i, h_text in enumerate(headers):
            cell = hdr_row.cells[i]
            cell.width = Inches(col_widths[i])
            cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
            set_cell_background(cell, HEX_PRIMARY_VIOLET)
            set_cell_margins(cell, top=120, bottom=120, left=140, right=140)
            set_cell_border(cell, 
                            top={"val": "single", "sz": "4", "color": HEX_PRIMARY_VIOLET},
                            bottom={"val": "single", "sz": "12", "color": HEX_SECONDARY_PINK},
                            left={"val": "none"}, right={"val": "none"})
            p = cell.paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            p.paragraph_format.space_after = Pt(0)
            r = p.add_run(h_text)
            r.bold = True
            r.font.name = "Segoe UI"
            r.font.size = Pt(9.5)
            r.font.color.rgb = RGBColor(255, 255, 255)
            
        # Data Rows
        for r_idx, row_data in enumerate(data):
            row = table.add_row()
            bg_color = HEX_LIGHT_GRAY_BG if r_idx % 2 == 1 else "FFFFFF"
            for c_idx, cell_value in enumerate(row_data):
                cell = row.cells[c_idx]
                cell.width = Inches(col_widths[c_idx])
                cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
                set_cell_background(cell, bg_color)
                set_cell_margins(cell, top=100, bottom=100, left=140, right=140)
                set_cell_border(cell, 
                                top={"val": "single", "sz": "4", "color": HEX_BORDER_GRAY},
                                bottom={"val": "single", "sz": "4", "color": HEX_BORDER_GRAY},
                                left={"val": "none"}, right={"val": "none"})
                p = cell.paragraphs[0]
                p.alignment = WD_ALIGN_PARAGRAPH.LEFT
                p.paragraph_format.space_after = Pt(0)
                r = p.add_run(cell_value)
                r.font.name = "Segoe UI"
                r.font.size = Pt(9)
                r.font.color.rgb = COLOR_DARK_TEXT
        
        doc.add_paragraph().paragraph_format.space_after = Pt(6)

    # =========================================================================
    # PORTADA (COVER PAGE)
    # =========================================================================
    p_logo = doc.add_paragraph()
    p_logo.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_logo.paragraph_format.space_before = Pt(36)
    p_logo.paragraph_format.space_after = Pt(18)
    
    logo_path = 'app/src/main/res/drawable/logo_kampai.png'
    if os.path.exists(logo_path):
        p_logo.add_run().add_picture(logo_path, width=Inches(2.5))
    
    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(12)
    p_title.paragraph_format.space_after = Pt(6)
    r_title = p_title.add_run("KAMPAI")
    r_title.bold = True
    r_title.font.name = "Segoe UI"
    r_title.font.size = Pt(34)
    r_title.font.color.rgb = COLOR_PRIMARY_VIOLET
    
    p_sub = doc.add_paragraph()
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sub.paragraph_format.space_after = Pt(24)
    r_sub = p_sub.add_run("MANUAL DE CONTEXTO EXHAUSTIVO Y ARQUITECTURA TÉCNICA")
    r_sub.bold = True
    r_sub.font.name = "Segoe UI"
    r_sub.font.size = Pt(13)
    r_sub.font.color.rgb = COLOR_SECONDARY_PINK

    p_desc = doc.add_paragraph()
    p_desc.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_desc.paragraph_format.space_after = Pt(40)
    r_desc = p_desc.add_run("Documentación Integral de la Aplicación Android: Arquitectura Clean MVVM, Minijuegos, Flujos UI/UX en Jetpack Compose, Multimedia, Persistencia, Monetización y Sistema de Jugadores.")
    r_desc.font.name = "Segoe UI"
    r_desc.font.size = Pt(10.5)
    r_desc.font.color.rgb = COLOR_MUTED_TEXT

    # Caja de metadatos de portada
    tbl_meta = doc.add_table(rows=1, cols=1)
    tbl_meta.alignment = WD_TABLE_ALIGNMENT.CENTER
    c_meta = tbl_meta.cell(0, 0)
    c_meta.width = Inches(5.5)
    set_cell_background(c_meta, HEX_LIGHT_VIOLET_BG)
    set_cell_margins(c_meta, top=160, bottom=160, left=200, right=200)
    set_cell_border(c_meta, 
                    top={"val": "single", "sz": "16", "color": HEX_PRIMARY_VIOLET},
                    bottom={"val": "single", "sz": "16", "color": HEX_SECONDARY_PINK},
                    left={"val": "single", "sz": "16", "color": HEX_PRIMARY_VIOLET},
                    right={"val": "single", "sz": "16", "color": HEX_SECONDARY_PINK})
    
    p_meta = c_meta.paragraphs[0]
    p_meta.paragraph_format.line_spacing = 1.3
    p_meta.paragraph_format.space_after = Pt(0)
    
    def add_meta_line(p, label, val):
        r1 = p.add_run(f"• {label}: ")
        r1.bold = True
        r1.font.name = "Segoe UI"
        r1.font.size = Pt(9.5)
        r1.font.color.rgb = COLOR_PRIMARY_VIOLET
        r2 = p.add_run(f"{val}\n")
        r2.font.name = "Segoe UI"
        r2.font.size = Pt(9.5)
        r2.font.color.rgb = COLOR_DARK_TEXT

    add_meta_line(p_meta, "Aplicación", "Kampai - Drinking Game")
    add_meta_line(p_meta, "Paquete Android", "com.jeremy.kampai")
    add_meta_line(p_meta, "Versión de Producción", "1.7 (VersionCode 8)")
    add_meta_line(p_meta, "Target SDK / Min SDK", "Android 15 (API 35) / Android 7.0 (API 24)")
    add_meta_line(p_meta, "Lenguaje y Runtime", "Kotlin 1.9+ / JVM Target 17")
    add_meta_line(p_meta, "Framework UI", "Jetpack Compose BOM (Material 3)")
    add_meta_line(p_meta, "Inyección de Dependencias", "Dagger Hilt 2.48")
    add_meta_line(p_meta, "Desarrollador / Autor", "Jeremy")
    add_meta_line(p_meta, "Estado del Proyecto", "Final / Producción Estable")

    doc.add_page_break()

    # =========================================================================
    # TABLA DE CONTENIDOS RESUMIDA
    # =========================================================================
    format_heading(doc, "ÍNDICE GENERAL DEL DOCUMENTO", level=1)
    
    toc_items = [
        ("1. Visión General y Propósito del Producto", "Definición, propuesta de valor, público objetivo y ficha técnica."),
        ("2. Arquitectura de Software y Stack Tecnológico", "Clean Architecture, MVVM, Unidirectional Data Flow, Hilt y Jetpack Compose."),
        ("3. Sistema de Gestión de Jugadores (Party Manager)", "Modelo de datos, avatares, indexación de color y persistencia JSON/Gson."),
        ("4. Catálogo y Análisis Exhaustivo de Minijuegos", "Reglas, lógica de estados, ViewModels y algoritmos de los 10 modos de juego."),
        ("5. Diseño Visual, UI/UX, Animaciones y Multimedia", "Sistema Aurora, Pager tipo Netflix, Glassmorphism, ExoPlayer y SoundManager."),
        ("6. Modelo de Negocio y Monetización (Google AdMob)", "Implementación de Banners, Interstitials por frecuencia y Rewarded Ads."),
        ("7. Internacionalización (i18n) y Recursos Localizados", "Soporte trilingüe (ES, EN, PT), +1.600 strings y cambio dinámico de idioma."),
        ("8. Configuración, Seguridad, Soporte y Aspectos Legales", "Disclaimer legal obligatorio, reporte de bugs con telemetría y políticas."),
        ("9. Síntesis y Recomendaciones de Evolución", "Fortalezas arquitectónicas y roadmap para versiones futuras.")
    ]
    
    tbl_toc = doc.add_table(rows=1, cols=2)
    style_table(tbl_toc, [2.5, 4.0], ["Sección", "Alcance y Temáticas Abordadas"], toc_items)

    # =========================================================================
    # SECCIÓN 1: VISIÓN GENERAL Y PROPÓSITO DEL PRODUCTO
    # =========================================================================
    format_heading(doc, "1. VISIÓN GENERAL Y PROPÓSITO DEL PRODUCTO", level=1)
    
    add_p(doc, 
          "Kampai es una solución móvil integral de entretenimiento social para dispositivos Android, concebida específicamente para dinamizar reuniones sociales, previas, fiestas y encuentros entre amigos mediante mecánicas lúdicas de juegos para beber (drinking games). Su nombre proviene del tradicional brindis japonés '¡Kampai!' (乾杯), que simboliza celebración compartida y camaradería.")
    
    add_p(doc, 
          "A diferencia de las aplicaciones genéricas del mercado que suelen limitarse a listas de texto estáticas o juegos individuales aislados, Kampai está diseñada como una suite de fiesta completa ('All-in-One Party Suite'). Integra en un único ecosistema altamente pulido más de 10 modos de juego distintos, combinando clásicos universales con dinámicas originales, un gestor inteligente de jugadores, efectos de sonido de alta fidelidad, fondos animados reactivos y soporte multilingüe completo.")

    add_callout(doc, 
                "Propuesta de Valor Central", 
                "Kampai elimina la fricción de tener que instalar múltiples aplicaciones o llevar cartas físicas a una reunión. Permite personalizar los participantes una sola vez en el Party Manager y disfrutar de dinámicas interactivas donde los nombres, géneros y avatares de los asistentes se integran orgánicamente en los retos, duelos y castigos.",
                border_color=HEX_PRIMARY_VIOLET, bg_color=HEX_LIGHT_VIOLET_BG, icon="🎯")

    format_heading(doc, "1.1. Ficha Técnica del Proyecto", level=2)
    
    tech_data = [
        ["Nombre de la Aplicación", "Kampai"],
        ["Package Name (ID)", "com.jeremy.kampai"],
        ["Versión Actual", "1.7 (Build 8)"],
        ["Sistema Operativo Base", "Android (Google Android OS)"],
        ["Min SDK Version", "API 24 (Android 7.0 Nougat) — ~98% compatibilidad global"],
        ["Compile & Target SDK", "API 35 (Android 15) — Estándar Google Play 2025/2026"],
        ["Lenguaje de Programación", "Kotlin (100%)"],
        ["Paradigma de UI", "Declarativo y Reactivo con Jetpack Compose Material 3"],
        ["Inyección de Dependencias", "Dagger Hilt (2.48) + Kapt"],
        ["Motor Multimedia", "Media3 ExoPlayer 1.2.0 + Android MediaPlayer"],
        ["Monetización Integrada", "Google Mobile Ads (AdMob 23.0.0)"],
        ["Idiomas Soportados", "Español (es), Inglés (en), Portugués (pt)"]
    ]
    tbl_tech = doc.add_table(rows=1, cols=2)
    style_table(tbl_tech, [2.5, 4.0], ["Parámetro Técnico", "Especificación / Detalle"], tech_data)

    # =========================================================================
    # SECCIÓN 2: ARQUITECTURA DE SOFTWARE Y STACK TECNOLÓGICO
    # =========================================================================
    format_heading(doc, "2. ARQUITECTURA DE SOFTWARE Y STACK TECNOLÓGICO", level=1)
    
    add_p(doc, 
          "El proyecto está estructurado bajo las directrices modernas de desarrollo recomendadas por Google (Modern Android Development - MAD), apoyándose en Clean Architecture y el patrón MVVM (Model - View - ViewModel) con Flujo Unidireccional de Datos (UDF).")

    format_heading(doc, "2.1. Capas Arquitectónicas", level=2)
    add_bullet(doc, "Capa de Presentación (UI / View)", "Construida íntegramente en Jetpack Compose con Material 3. Es 100% reactiva y stateless en la medida de lo posible: observa StateFlows emitidos por los ViewModels y despacha intenciones de usuario (User Intents / Callbacks) hacia los mismos.")
    add_bullet(doc, "Capa de Dominio (Domain / ViewModel)", "Aloja la lógica de estado de cada juego y las reglas de negocio. Cada pantalla cuenta con su propio HiltViewModel (@HiltViewModel) que gestiona corrutinas en el viewModelScope y emite estados inmutables a través de StateFlow.")
    add_bullet(doc, "Capa de Datos (Data / Repositories)", "Gestiona las fuentes de información: SharedPreferences con Gson (para el PartyRepository), AndroidX DataStore Preferences (para ThemeManager y WarmupPreferencesManager), y el framework de Resources de Android para bancos masivos de preguntas y categorías.")

    format_heading(doc, "2.2. Inyección de Dependencias con Dagger Hilt", level=2)
    add_p(doc, 
          "KampaiApplication está anotada con @HiltAndroidApp, sirviendo como raíz del árbol de dependencias. MainActivity está anotada con @AndroidEntryPoint y los ViewModels se inyectan mediante hiltViewModel(). Se proveen como dependencias Singleton componentes clave como SoundManager, ThemeManager, PartyRepository y GameRepository, garantizando una gestión de memoria óptima y ciclo de vida predecible.")

    format_heading(doc, "2.3. Sistema de Navegación Robusto y Resiliente", level=2)
    add_p(doc, 
          "La navegación utiliza Jetpack Navigation Compose mediante un NavHost centralizado en MainActivity.kt. Para prevenir errores comunes en Compose (como el típico cuelgue o pantalla negra por doble pulsación rápida sobre botones de retroceso o tarjetas), se implementaron extensiones personalizadas en NavigationExtension.kt:")
    
    add_bullet(doc, "navigateSafe(route: String)", "Comprueba que la ruta de destino no sea idéntica a la ruta actual del BackStackEntry antes de disparar la transición, evitando duplicación en el stack.")
    add_bullet(doc, "popBackStackSafe(): Boolean", "Verifica la existencia de un previousBackStackEntry válido antes de ordenar la navegación hacia atrás, capturando excepciones de concurrencia.")

    add_callout(doc, 
                "Animaciones Cinematográficas de Navegación", 
                "El NavHost incorpora transiciones horizontales animadas con tween(300): slideInHorizontally + fadeIn al ingresar a un minijuego, y slideOutHorizontally + fadeOut al regresar, proporcionando una experiencia fluida tipo aplicación nativa de alta gama.",
                border_color=HEX_CYAN, bg_color=HEX_LIGHT_CYAN_BG, icon="🎬")

    # =========================================================================
    # SECCIÓN 3: SISTEMA DE GESTIÓN DE JUGADORES (PARTY MANAGER)
    # =========================================================================
    format_heading(doc, "3. SISTEMA DE GESTIÓN DE JUGADORES (PARTY MANAGER)", level=1)
    
    add_p(doc, 
          "Uno de los pilares diferenciales de Kampai es el Party Manager (Gestionar Jugadores). En lugar de requerir que los participantes introduzcan sus nombres en cada partida, Kampai mantiene un estado persistente de la 'Party' (el grupo de amigos) accesible desde el FAB flotante y la cabecera del Home.")

    format_heading(doc, "3.1. Modelo de Datos del Jugador (PlayerModel)", level=2)
    add_p(doc, "El jugador está definido por una data class inmutable con los siguientes atributos:")
    add_bullet(doc, "id: String", "Identificador único universal generado mediante UUID.randomUUID().")
    add_bullet(doc, "name: String", "Nombre o apodo del participante, validado para no permitir cadenas vacías.")
    add_bullet(doc, "gender: Gender", "Enum con tres opciones inclusivas: MALE ('👨'), FEMALE ('👩') y OTHER ('👽').")
    add_bullet(doc, "colorIndex: Int", "Índice que asigna automáticamente una tonalidad distintiva de la paleta oficial (Violeta, Rosa, Cyan, Rojo, Ámbar, Verde Esmeralda, Púrpura, etc.).")
    add_bullet(doc, "avatarEmoji: String", "Emoji personalizado asignado al jugador. Por defecto se sortea uno aleatorio de un catálogo de más de 100 emojis organizados en 4 familias: Animales, Rostros/Expresiones, Fantasía y Deportes.")

    format_heading(doc, "3.2. Persistencia y Resiliencia en PartyRepository", level=2)
    add_p(doc, 
          "PartyRepository utiliza SharedPreferences privadas ('kampai_prefs') serializando la lista de jugadores a JSON mediante Google Gson. Incluye mecanismos de migración en caliente (por ejemplo, si se leen jugadores antiguos sin avatar, les asigna uno automáticamente sin corromper el almacenamiento) y manejo de excepciones que purga datos defectuosos en caso de fallos de parsing.")

    format_heading(doc, "3.3. Integración Dinámica en los Minijuegos", level=2)
    add_p(doc, 
          "La lista de jugadores no es cosmética: se inyecta en los minijuegos activos para personalizar la experiencia:")
    add_bullet(doc, "WarmUp / PartyMix", "Sortea jugadores para retos específicos, empareja hombres contra mujeres en duelos de Piedra-Papel-Tijera, y asigna roles únicos como 'El Juez' o 'La Gallina Ciega'.")
    add_bullet(doc, "Verdad o Reto", "Gestiona una cola equitativa con barajado (round-robin) para que todos los jugadores participen por igual sin repeticiones consecutivas.")
    add_bullet(doc, "El Impostor", "Asigna en secreto el rol de impostor a uno de los participantes del grupo.")
    add_bullet(doc, "King's Cup", "Rota los turnos de extracción de carta indicando visualmente a qué jugador le corresponde levantar carta.")

    # =========================================================================
    # SECCIÓN 4: CATÁLOGO Y ANÁLISIS EXHAUSTIVO DE MINIJUEGOS
    # =========================================================================
    format_heading(doc, "4. CATÁLOGO Y ANÁLISIS EXHAUSTIVO DE MINIJUEGOS", level=1)
    
    add_p(doc, 
          "Kampai incluye 10 modalidades de juego distintas, clasificadas por su nivel de intensidad alcohólica y social mediante el enum GameIntensity (Suave, Intermedio, Fuerte, Extremo). Cada minijuego responde a una necesidad psicológica y social diferente durante una fiesta, desde 'rompehielos' para iniciar la velada hasta dinámicas de alta presión competitiva.")

    # Tabla Maestra de Minijuegos
    games_master = [
        ["PartyMix (WarmUp)", "🍹", "2+ jugadores", "Intermedio", "game_warmup", "Modo insignia con rondas configurables, 116 frases y 11 eventos sorpresa."],
        ["Cultura Chupística", "🤓", "2+ jugadores", "Suave", "culture_selection", "Clásico de agilidad mental con 113 categorías o Modo Bomba contrarreloj."],
        ["King's Cup", "👑", "3-8 jugadores", "Fuerte", "game_kingscup", "El legendario juego de naipes de fiesta con reglas universales y copa central."],
        ["El Impostor", "👺", "4+ jugadores", "Fuerte", "game_impostor", "Deducción social y engaño (Spyfall). Un infiltrado no conoce la palabra."],
        ["Yo Nunca Nunca", "🙊", "2+ jugadores", "Intermedio", "game_never", "222 confesiones con tarjetas deslizables estilo Tinder."],
        ["Verdad o Reto", "🎭", "2-10 jugadores", "Intermedio", "game_truth", "284 desafíos íntimos y cómicos cargados dinámicamente por reflexión."],
        ["Mayor o Menor", "🃏", "2+ jugadores", "Fuerte", "game_highlow", "Predicción de cartas numéricas consecutivas con contador de rachas."],
        ["Mímica Borracha", "🤸", "4+ jugadores", "Suave", "game_charades", "Dígalo con mímica bajo presión de tiempo con 3 dificultades y bonos."],
        ["Ruleta Rusa", "🧨", "2+ jugadores", "Extremo", "game_roulette", "Tambor de revólver giratorio con 1 bala letal y medidor de tensión."],
        ["¿Quién es más probable?", "🫵", "3+ jugadores", "Intermedio", "game_likely", "319 preguntas de juicio grupal donde todos apuntan al culpable."]
    ]
    tbl_master = doc.add_table(rows=1, cols=6)
    style_table(tbl_master, [1.5, 0.5, 1.0, 0.9, 1.1, 2.0], 
                ["Minijuego", "Icono", "Jugadores", "Intensidad", "Ruta Compose", "Mecánica Principal"], games_master)

    # 4.1 PartyMix (WarmUp)
    format_heading(doc, "4.1. PartyMix (WarmUp) — Modo Insignia", level=2)
    add_p(doc, 
          "PartyMix es el juego más avanzado y complejo de Kampai. Combina más de 116 consignas grupales rápidas con 11 eventos interactivos especiales de alta tensión. Utiliza un fondo de video nocturno en bucle gestionado por ExoPlayer.", bold_prefix="Concepto y Atmósfera: ")
    
    duelo_img = 'app/src/main/res/drawable/duelo.png'
    if os.path.exists(duelo_img):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(4)
        p_img.paragraph_format.space_after = Pt(8)
        p_img.add_run().add_picture(duelo_img, width=Inches(3.2))

    add_p(doc, 
          "Al ingresar, los usuarios pueden personalizar la duración de la partida (de 5 a 100 rondas, por defecto 20) y la frecuencia con la que aparecen eventos sorpresa (cada 2 a 10 rondas, por defecto cada 4).", bold_prefix="Configuración Flexible: ")

    add_bullet(doc, "SHOT_CHALLENGE", "Reto de shot inmediato. Si el jugador lo rechaza, recibe una penalización fija de -2 tragos.")
    add_bullet(doc, "SPEED_TEST", "Desafío de agilidad y reflejos bajo cuenta regresiva implacable.")
    add_bullet(doc, "RPS_DUEL", "Duelo de Piedra, Papel o Tijera. Si hay hombres y mujeres en el grupo, empareja un hombre contra una mujer; si no, elige 2 jugadores aleatorios.")
    add_bullet(doc, "STARING_CONTEST", "Duelo de miradas fijas entre dos jugadores designados; el primero que parpadee o se ría, bebe.")
    add_bullet(doc, "THE_JUDGE", "Un jugador es coronado como 'El Juez' y dicta sentencias inapelables durante la ronda.")
    add_bullet(doc, "GIFT (El Regalo Misterioso)", "Dinámica en 2 fases: primero los jugadores que quieran el regalo deben levantar la mano en la vida real (GiftPhase.RAISE_HAND). Luego se presiona 'Revelar Regalo' (GiftPhase.REVEAL), desvelando si el regalo es positivo (repartir tragos) o negativo (tomar fondo blanco).")
    add_bullet(doc, "ICE_PASS", "Pasar un cubito de hielo de boca en boca por toda la ronda sin usar las manos.")
    add_bullet(doc, "BLIND_CHICKEN", "El jugador designado se venda los ojos, toma el teléfono y dispara fotos al azar durante 1 minuto mientras los demás huyen. Todos los que aparezcan en las fotos deben beber.")
    add_bullet(doc, "SELFIE", "Selfie grupal obligatoria en menos de 10 segundos o penalización general.")
    add_bullet(doc, "MEDUSA", "Todos bajan la cabeza hacia la mesa. A la cuenta de tres miran a alguien: si dos jugadores cruzan miradas, gritan '¡MEDUSA!' y ambos beben.")

    add_p(doc, 
          "WarmUpViewModel mantiene un mapa reactivo (_drinkStats) que contabiliza en tiempo real los tragos acumulados por cada jugador. Se dispone de un diálogo de estadísticas flotante donde se pueden sumar tragos manualmente.", bold_prefix="Sistema de Conteo y Estadísticas: ")

    add_p(doc, 
          "Al completarse las rondas, la app presenta la pantalla de 'Resultados de la Noche': corona al Rey/Reina de la Copa (quien más bebió), al 'Más Santo' (quien menos bebió) y emite 'La Sentencia Final', donde el más sobrio o el más ebrio asigna una penitencia final al perdedor.", bold_prefix="Ceremonia de Clausura: ")

    # 4.2 Cultura Chupística
    format_heading(doc, "4.2. Cultura Chupística (Modo Clásico y Modo Bomba)", level=2)
    add_p(doc, 
          "Inspirado en el clásico juego social 'Cultura Chupística pide...'. En la pantalla de selección, los jugadores eligen entre dos modalidades:")

    culture_img = 'app/src/main/res/drawable/culture.png'
    if os.path.exists(culture_img):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(4)
        p_img.paragraph_format.space_after = Pt(8)
        p_img.add_run().add_picture(culture_img, width=Inches(3.2))

    add_bullet(doc, "Modo Clásico", "Presenta una categoría aleatoria extraída de un banco de 113 categorías culturales (marcas, canciones, películas, ciudades, etc.). Los jugadores dicen elementos por turno en el sentido de las agujas del reloj. Quien repita o demore, bebe.")
    add_bullet(doc, "Modo Bomba (BombGameScreen)", "Añade una bomba de tiempo con fusible invisible sorteado aleatoriamente entre 30 y 60 segundos. Los jugadores se van pasando el teléfono diciendo elementos válidos. El motor de sonido (SoundManager) reproduce un 'tic-tac' que acelera conforme se agota el tiempo (de intervalos de 1100ms hasta 250ms), culminando con una explosión (R.raw.explosion), pantalla roja intermitente y vibración háptica.")

    # 4.3 King's Cup
    format_heading(doc, "4.3. King's Cup (Copa del Rey)", level=2)
    add_p(doc, 
          "Recreación digital de la baraja inglesa completa de 52 cartas barajada proceduralmente. Los jugadores tocan la baraja y una animación tridimensional revela la carta extraída:")
    
    kings_rules = [
        ["As (A)", "Cascada (Waterfall)", "Todos comienzan a beber al mismo tiempo; nadie puede detenerse hasta que quien levantó la carta pare."],
        ["2", "Eliges Tú (You)", "El jugador designa a cualquier participante para que tome 2 tragos."],
        ["3", "Bebo Yo (Me)", "Quien levantó la carta bebe un trago."],
        ["4", "Chicas (Ladies)", "Todas las mujeres presentes beben."],
        ["5", "El Pulgar (Thumbmaster)", "El jugador apoya discretamente el pulgar en la mesa. El último en imitarlo bebe."],
        ["6", "Chicos (Men)", "Todos los hombres presentes beben."],
        ["7", "Manos al Cielo (Heaven)", "Todos levantan las manos rápidamente. El último en hacerlo bebe."],
        ["8", "Compañero (Mate)", "Elige un compañero de tragos: cada vez que uno deba beber, el otro también bebe."],
        ["9", "Rima (Rhyme)", "El jugador dice una palabra y los demás deben rimar rápidamente."],
        ["10", "Categorías (Categories)", "Se elige un tema y se nombran elementos por turno hasta que alguien falle."],
        ["Jota (J)", "Crea una Regla (Make a Rule)", "Se inventa una norma que rige hasta el final de la partida."],
        ["Reina (Q)", "Preguntas (Questions)", "Solo se puede hablar haciendo preguntas; responder directamente penaliza con trago."],
        ["Rey (K)", "La Copa del Rey (King's Cup)", "Reyes 1 a 3 vierten parte de su bebida en el vaso central. ¡El 4to Rey debe beber la copa entera!"]
    ]
    tbl_kings = doc.add_table(rows=1, cols=3)
    style_table(tbl_kings, [1.0, 2.0, 3.5], ["Carta", "Regla", "Mecánica de Juego"], kings_rules)

    # 4.4 El Impostor
    format_heading(doc, "4.4. El Impostor — Deducción Social", level=2)
    add_p(doc, 
          "Adaptación de juegos de deducción y engaño tipo 'Spyfall' o 'Undercover' para 3 o más participantes:", bold_prefix="Dinámica: ")
    add_bullet(doc, "Fase Secreta (Pass & Play)", "La app sortea una palabra secreta de un banco de 90 palabras y elige a un impostor en secreto. El teléfono se pasa de mano en mano; los inocentes leen la palabra y el impostor ve únicamente '???'.")
    add_bullet(doc, "Fase de Pistas", "Cada jugador dice en voz alta una palabra relacionada. El impostor debe disimular sin conocer el tema real.")
    add_bullet(doc, "Fase de Votación y Revelación", "La app ofrece una pantalla de votación interactiva donde se eligen los sospechosos. Se revela si el grupo atrapó al impostor o si este logró escapar.")

    # 4.5 Yo Nunca Nunca
    format_heading(doc, "4.5. Yo Nunca Nunca (Never Have I Ever)", level=2)
    add_p(doc, 
          "Colección masiva de 222 preguntas picantes, atrevidas y cómicas. Destaca por su implementación de gestos táctiles tipo Tinder (detectHorizontalDragGestures): la tarjeta responde a la física del dedo, rotando sobre su eje y permitiendo deslizar hacia los lados para avanzar de tarjeta con un feedback dinámico.")

    # 4.6 Verdad o Reto
    format_heading(doc, "4.6. Verdad o Reto (Truth or Dare)", level=2)
    add_p(doc, 
          "Contiene 284 desafíos (161 Verdades y 123 Retos). Destaca por su arquitectura técnica en TruthViewModel: en lugar de cargar listas fijas en memoria, utiliza reflexión en Kotlin sobre la clase R.string::class.java.fields para indexar dinámicamente todas las claves que comiencen con 'truth_' y 'dare_', garantizando escalabilidad inmediata al añadir nuevas preguntas en los XML.")

    # 4.7 Mayor o Menor
    format_heading(doc, "4.7. Mayor o Menor (High / Low)", level=2)
    add_p(doc, 
          "Juego rápido de cartas numéricas (valores del 1 al 13). El usuario predice si la siguiente carta será mayor o menor. La app registra la racha consecutiva (streak) de aciertos y muestra retroalimentación visual de comparación.")

    # 4.8 Mímica Borracha
    format_heading(doc, "4.8. Mímica Borracha (Charades)", level=2)
    add_p(doc, 
          "Juego de actuación con 3 dificultades: Fácil (60s, 1 pt), Medio (45s, 2 pts) y Difícil (30s, 3 pts). Cuenta con bonificación de velocidad: si se adivina antes de transcurrido el 30% del tiempo total, se suma un punto extra. Si se pasa de palabra, se resta un punto.")

    # 4.9 Ruleta Rusa
    format_heading(doc, "4.9. Ruleta Rusa (Russian Roulette)", level=2)
    add_p(doc, 
          "Simulador de tambor de revólver de 6 recámaras con una sola bala letal aleatoria. Incluye simulación física de giro (spinCylinder) con paso angular y deceleración, junto a un 'Medidor de Tensión' reactivo que escala de 0% al 95% a medida que quedan menos recámaras seguras.")

    # 4.10 ¿Quién es más probable?
    format_heading(doc, "4.10. ¿Quién es más probable? (Most Likely To)", level=2)
    add_p(doc, 
          "Banco de 319 preguntas de juicio social y debate. A la de tres, todos señalan con el dedo al culpable en la vida real. Quien acumule más votos en la mesa, bebe.")

    # =========================================================================
    # SECCIÓN 5: DISEÑO VISUAL, EXPERIENCIA DE USUARIO (UI/UX) Y MULTIMEDIA
    # =========================================================================
    format_heading(doc, "5. DISEÑO VISUAL, UI/UX, ANIMACIONES Y MULTIMEDIA", level=1)
    
    add_p(doc, 
          "La identidad visual de Kampai rompe radicalmente con las interfaces genéricas de utilidades. Apuesta por una dirección de arte 'Dark Cyberpunk Party' con acabados prémium, transparencias y motion graphics procedurales.")

    format_heading(doc, "5.1. Sistema Aurora Background con Parallax", level=2)
    add_p(doc, 
          "El fondo de HomeScreen (KampaiAuroraBackground) se dibuja en tiempo real sobre dos capas de Canvas nativas:")
    add_bullet(doc, "Capa Estelar y Partículas", "Dibuja 18 partículas estáticas con centelleo senoidal asíncrono para dar textura de profundidad cósmica.")
    add_bullet(doc, "Capa de Orbes Líquidos", "Cuatro emisores radiales con desenfoque gaussiano de 72.dp que orbitan a diferentes frecuencias angulares. El color predominante muta suavemente (animación tween de 1200ms) adaptándose al juego enfocado en el carrusel.")
    add_bullet(doc, "Efecto Parallax", "El fondo se traslada verticalmente a una velocidad del 35% respecto al scroll del usuario, generando sensación de profundidad 3D.")

    format_heading(doc, "5.2. Carrusel Estilo Plataformas de Streaming (Netflix Shelves)", level=2)
    add_p(doc, 
          "HomeScreen implementa una arquitectura de navegación vertical que contiene estantes horizontales independientes:")
    add_bullet(doc, "Fila Hero (Más Jugados)", "Utiliza HorizontalPager centrado con escala dinámica (lerp de 0.90f a 1.0f) y rotación sobre el eje Y (rotationY) de las tarjetas laterales, resaltando el título activo.")
    add_bullet(doc, "Filas Secundarias (Recién Añadido y Clásicos)", "LazyRows compactas con tarjetas redondeadas de 148x178 dp, insignias 'NUEVO' y respuestas táctiles al presionar.")
    add_bullet(doc, "Fila Próximamente", "Tarjetas interactivas con candado que permiten al usuario enviar sugerencias de nuevos juegos por correo electrónico directamente al creador.")

    format_heading(doc, "5.3. Motor de Audio y Efectos Sonoros (SoundManager)", level=2)
    add_p(doc, 
          "SoundManager gestiona la reproducción de clips de audio almacenados en res/raw (como tic_tac.mp3 y explosion.wav) utilizando MediaPlayer, liberando recursos automáticamente al completar la reproducción (setOnCompletionListener) para prevenir fugas de memoria.")

    # =========================================================================
    # SECCIÓN 6: MONETIZACIÓN Y MODELO DE NEGOCIO (GOOGLE ADMOB)
    # =========================================================================
    format_heading(doc, "6. MODELO DE NEGOCIO Y MONETIZACIÓN (GOOGLE ADMOB)", level=1)
    
    add_p(doc, 
          "Kampai implementa un modelo Free-to-Play ético financiado mediante publicidad programática no invasiva a través de Google Mobile Ads SDK (com.google.android.gms:play-services-ads:23.0.0):")

    add_bullet(doc, "Banner Ads (AdView)", "Ubicados discretamente en el pie de página de HomeScreen y de minijuegos clásicos (AdUnit: ca-app-pub-5499036778442602/1480575304), delimitados con un contenedor estilizado y etiqueta 'ANUNCIO'.")
    add_bullet(doc, "Interstitial Ads (Pantalla Completa)", "Controlados por un contador inteligente (classicsClickCount). Solo se muestran cada 3 transiciones hacia minijuegos o al terminar partidas largas (>20 rondas en WarmUp), garantizando que no se interrumpa el ritmo de la fiesta.")
    add_bullet(doc, "Rewarded Video Ads (Apoyar al Creador)", "En SettingsScreen se ofrece la opción voluntaria de ver un anuncio de video bonificado (ca-app-pub-5499036778442602/5835409824). Al completarse, premia al usuario con un Toast de agradecimiento ('¡Muchas gracias por tu apoyo! ❤️ Esto ayuda a seguir mejorando Kampai.').")

    # =========================================================================
    # SECCIÓN 7: INTERNACIONALIZACIÓN (I18N) Y LOCALIZACIÓN
    # =========================================================================
    format_heading(doc, "7. INTERNACIONALIZACIÓN (I18N) Y GESTIÓN DE CONTENIDOS", level=1)
    
    add_p(doc, 
          "La aplicación cuenta con internacionalización completa nativa para 3 idiomas, con más de 1.600 recursos de texto traducidos minuciosamente con lenguaje coloquial festivo adecuado a cada región:")
    
    i18n_data = [
        ["values/strings.xml", "Español (ES/LATAM)", "829 elementos", "Idioma base del proyecto. Tono cercano, divertido y festivo."],
        ["values-en/strings.xml", "Inglés (EN/US)", "792 elementos", "Traducción completa para mercado global y angloparlante."],
        ["values-pt/strings.xml", "Portugués (PT/BR)", "792 elementos", "Localización para Brasil y Portugal con jergas de fiesta."]
    ]
    tbl_i18n = doc.add_table(rows=1, cols=4)
    style_table(tbl_i18n, [1.8, 1.8, 1.2, 1.7], ["Archivo de Recursos", "Idioma / Región", "Entradas", "Observaciones"], i18n_data)

    add_p(doc, 
          "A través de SettingsViewModel, el usuario puede conmutar el idioma en caliente sin necesidad de reiniciar la app ni cambiar el idioma del sistema operativo, apoyándose en la API oficial de Android AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLanguage.code)).")

    # =========================================================================
    # SECCIÓN 8: CONFIGURACIÓN, SEGURIDAD Y COMPLIANCE
    # =========================================================================
    format_heading(doc, "8. CONFIGURACIÓN, SEGURIDAD, SOPORTE Y ASPECTOS LEGALES", level=1)
    
    add_p(doc, 
          "Kampai cumple con estrictos estándares de responsabilidad social y directrices de publicación en Google Play Store:")
    
    add_bullet(doc, "Descargo de Responsabilidad (Disclaimer Dialog)", "Se presenta de manera obligatoria al primer arranque y no puede cerrarse hasta que el usuario pulse 'Aceptar'. Advierte sobre la edad legal para consumir alcohol, prohíbe taxativamente conducir tras beber y promueve el consumo responsable.")
    add_bullet(doc, "Canal de Soporte y Reporte de Fallos", "SettingsScreen incluye botones directos para enviar sugerencias y reportes de errores a través de un intent de correo electrónico (game.kampai@gmail.com). El reporte adjunta automáticamente la marca, modelo de dispositivo y versión de Android SDK del usuario.")
    add_bullet(doc, "Políticas de Privacidad y Términos", "Enlaces funcionales a documentos legales publicados en la nube que explican el tratamiento de datos y la exención de responsabilidad.")

    # =========================================================================
    # SECCIÓN 9: CONCLUSIONES Y ROADMAP
    # =========================================================================
    format_heading(doc, "9. SÍNTESIS Y RECOMENDACIONES DE EVOLUCIÓN", level=1)
    
    add_p(doc, 
          "Kampai destaca como una aplicación madura, técnicamente sólida y visualmente sobresaliente. Su arquitectura Clean MVVM en Kotlin y Jetpack Compose asegura un mantenimiento sencillo y alta extensibilidad. Como posibles vías de evolución para versiones futuras se identifican:")
    
    add_bullet(doc, "Modo Multijugador Multi-Dispositivo (Party Sync)", "Conexión local vía WebSocket, Nearby Connections o Firebase Realtime Database para que cada participante pueda jugar y votar desde su propio smartphone.")
    add_bullet(doc, "Editor Personalizado de Mazos y Retos", "Permitir a los usuarios redactar sus propias tarjetas de 'Yo Nunca' y 'Verdad o Reto' guardándolas en base de datos local Room.")
    add_bullet(doc, "Métricas Avanzadas de Fiesta", "Generación de infografías descargables o compartibles en Instagram Stories al terminar una partida con el resumen de la noche.")

    # Guardar documento final
    output_filename = "Documento_Contexto_Completo_Kampai.docx"
    doc.save(output_filename)
    print(f"Documento generado exitosamente: {output_filename}")

if __name__ == "__main__":
    create_document()
