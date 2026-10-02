from pptx import Presentation
from pptx.enum.text import PP_ALIGN
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE


OUT_PATH = r"c:\Users\thabi\StudioProjects\hustlefix\.artifacts\HustleFix_Presentation.pptx"

prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)

NAVY = RGBColor(12, 29, 48)
BLUE = RGBColor(15, 118, 196)
ORANGE = RGBColor(245, 130, 32)
LIGHT = RGBColor(244, 247, 250)
TEXT = RGBColor(39, 52, 67)
WHITE = RGBColor(255, 255, 255)
GRAY = RGBColor(105, 120, 137)
GREEN = RGBColor(38, 166, 91)


def set_bg(slide, color=LIGHT):
    fill = slide.background.fill
    fill.solid()
    fill.fore_color.rgb = color


def add_title(slide, title, subtitle=None):
    bar = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, Inches(0.72))
    bar.fill.solid()
    bar.fill.fore_color.rgb = NAVY
    bar.line.fill.background()

    title_box = slide.shapes.add_textbox(Inches(0.6), Inches(1.0), Inches(11.8), Inches(0.8))
    tf = title_box.text_frame
    p = tf.paragraphs[0]
    run = p.runs[0] if p.runs else p.add_run()
    run.text = title
    run.font.size = Pt(28)
    run.font.bold = True
    run.font.color.rgb = NAVY

    if subtitle:
        sub_box = slide.shapes.add_textbox(Inches(0.6), Inches(1.8), Inches(11.0), Inches(0.45))
        tf2 = sub_box.text_frame
        p2 = tf2.paragraphs[0]
        run2 = p2.runs[0] if p2.runs else p2.add_run()
        run2.text = subtitle
        run2.font.size = Pt(15)
        run2.font.color.rgb = GRAY


def add_bullets(slide, bullets, left=Inches(0.8), top=Inches(1.8), width=Inches(11.2), font_size=22):
    box = slide.shapes.add_textbox(left, top, width, Inches(4.8))
    tf = box.text_frame
    tf.word_wrap = True
    tf.margin_left = 0
    tf.margin_right = 0
    tf.margin_top = 0
    tf.margin_bottom = 0
    for i, bullet in enumerate(bullets):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.text = bullet
        p.level = 0
        p.bullet = True
        p.alignment = PP_ALIGN.LEFT
        p.space_after = Pt(10)
        for r in p.runs:
            r.font.size = Pt(font_size)
            r.font.color.rgb = TEXT
            r.font.name = 'Aptos'


def add_footer(slide, slide_no):
    footer = slide.shapes.add_textbox(Inches(11.9), Inches(6.95), Inches(0.8), Inches(0.25))
    tf = footer.text_frame
    p = tf.paragraphs[0]
    run = p.runs[0] if p.runs else p.add_run()
    run.text = str(slide_no)
    run.font.size = Pt(12)
    run.font.bold = True
    run.font.color.rgb = NAVY


def add_cover_slide():
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_bg(slide, WHITE)

    top_band = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, Inches(0.9))
    top_band.fill.solid()
    top_band.fill.fore_color.rgb = NAVY
    top_band.line.fill.background()

    title_box = slide.shapes.add_textbox(Inches(0.7), Inches(1.5), Inches(11.5), Inches(1.1))
    tf = title_box.text_frame
    p = tf.paragraphs[0]
    r = p.runs[0] if p.runs else p.add_run()
    r.text = 'HustleFix'
    r.font.size = Pt(30)
    r.font.bold = True
    r.font.color.rgb = NAVY

    sub_box = slide.shapes.add_textbox(Inches(0.7), Inches(2.45), Inches(8.5), Inches(0.7))
    tf2 = sub_box.text_frame
    p2 = tf2.paragraphs[0]
    r2 = p2.runs[0] if p2.runs else p2.add_run()
    r2.text = 'Android Service Marketplace Application'
    r2.font.size = Pt(20)
    r2.font.color.rgb = TEXT

    detail_box = slide.shapes.add_textbox(Inches(0.7), Inches(3.4), Inches(7.5), Inches(2.1))
    tf3 = detail_box.text_frame
    lines = [
        'Detailed System Design & Technical Implementation',
        '',
        'Presenter: [Your Name / Team Names]',
        'Supervisor: [Supervisor Name]'
    ]
    for idx, line in enumerate(lines):
        p3 = tf3.paragraphs[0] if idx == 0 else tf3.add_paragraph()
        p3.text = line
        p3.alignment = PP_ALIGN.LEFT
        for run in p3.runs:
            run.font.size = Pt(18 if idx in (0, 2, 3) else 14)
            run.font.color.rgb = TEXT if idx != 0 else BLUE
            run.font.bold = idx == 0

    # Faint app mockup panel
    mock = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(9.2), Inches(1.7), Inches(3.0), Inches(4.3))
    mock.fill.solid()
    mock.fill.fore_color.rgb = LIGHT
    mock.line.color.rgb = BLUE
    mock.line.width = Pt(2)
    inner = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(9.6), Inches(2.3), Inches(2.2), Inches(0.7))
    inner.fill.solid(); inner.fill.fore_color.rgb = ORANGE; inner.line.fill.background()
    inner2 = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(9.6), Inches(3.4), Inches(2.2), Inches(0.8))
    inner2.fill.solid(); inner2.fill.fore_color.rgb = BLUE; inner2.line.fill.background()
    inner3 = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(9.6), Inches(4.6), Inches(2.2), Inches(0.7))
    inner3.fill.solid(); inner3.fill.fore_color.rgb = GREEN; inner3.line.fill.background()

    add_footer(slide, 1)


def add_standard_slide(title, bullets, slide_no, subtitle=None):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_bg(slide, WHITE)
    add_title(slide, title, subtitle)
    add_bullets(slide, bullets)
    add_footer(slide, slide_no)


def add_architecture_slide():
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_bg(slide, WHITE)
    add_title(slide, 'System Architecture', 'Client-server model with cloud services and payment gateway')

    client = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(2.3), Inches(2.7), Inches(1.8))
    client.fill.solid(); client.fill.fore_color.rgb = BLUE; client.line.fill.background()
    client_tf = client.text_frame; client_tf.text = 'Android Client\nJetpack Compose\nViewModels'
    for p in client_tf.paragraphs:
        for r in p.runs:
            r.font.size = Pt(16)
            r.font.color.rgb = WHITE
            r.font.bold = True
            p.alignment = PP_ALIGN.CENTER

    server = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(4.7), Inches(2.3), Inches(3.2), Inches(1.8))
    server.fill.solid(); server.fill.fore_color.rgb = ORANGE; server.line.fill.background()
    server_tf = server.text_frame; server_tf.text = 'Cloud Backend\nFirebase Auth\nFirestore\nFCM'
    for p in server_tf.paragraphs:
        for r in p.runs:
            r.font.size = Pt(16)
            r.font.color.rgb = WHITE
            r.font.bold = True
            p.alignment = PP_ALIGN.CENTER

    payfast = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(8.8), Inches(2.3), Inches(3.2), Inches(1.8))
    payfast.fill.solid(); payfast.fill.fore_color.rgb = GREEN; payfast.line.fill.background()
    payfast_tf = payfast.text_frame; payfast_tf.text = 'External Gateway\nPayFast API'
    for p in payfast_tf.paragraphs:
        for r in p.runs:
            r.font.size = Pt(17)
            r.font.color.rgb = WHITE
            r.font.bold = True
            p.alignment = PP_ALIGN.CENTER

    conn1 = slide.shapes.add_shape(MSO_SHAPE.RIGHT_ARROW, Inches(3.5), Inches(3.1), Inches(1.1), Inches(0.5))
    conn1.fill.solid(); conn1.fill.fore_color.rgb = NAVY; conn1.line.fill.background()
    conn2 = slide.shapes.add_shape(MSO_SHAPE.RIGHT_ARROW, Inches(8.0), Inches(3.1), Inches(0.7), Inches(0.5))
    conn2.fill.solid(); conn2.fill.fore_color.rgb = NAVY; conn2.line.fill.background()

    add_footer(slide, 5)


def add_layered_architecture_slide():
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_bg(slide, WHITE)
    add_title(slide, 'Software Architecture & Layering', 'Clean separation of concerns')

    layers = [
        ('Presentation Layer', 'Jetpack Compose UI screens\nNavigation Graph'),
        ('Business Logic Layer', 'ViewModels\nRepositories'),
        ('Data Layer', 'Firestore collections\nLocal session cache'),
    ]
    x_positions = [0.9, 4.5, 8.1]
    for idx, (title, body) in enumerate(layers):
        box = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(x_positions[idx]), Inches(2.2), Inches(3.1), Inches(2.8))
        box.fill.solid(); box.fill.fore_color.rgb = BLUE if idx == 0 else ORANGE if idx == 1 else GREEN; box.line.fill.background()
        tf = box.text_frame; tf.text = f'{title}\n\n{body}'
        for p in tf.paragraphs:
            for r in p.runs:
                r.font.size = Pt(18)
                r.font.color.rgb = WHITE
                r.font.bold = p.level == 0
                p.alignment = PP_ALIGN.CENTER

    add_footer(slide, 6)


def add_payment_workflow_slide():
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_bg(slide, WHITE)
    add_title(slide, 'Booking Vault & Payment Workflow', 'Secure fund holding and milestone-based release')

    step1 = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(2.0), Inches(3.4), Inches(1.7))
    step1.fill.solid(); step1.fill.fore_color.rgb = BLUE; step1.line.fill.background()
    step1_tf = step1.text_frame; step1_tf.text = 'Step 1\nClient confirms booking\nand pays through PayFast or wallet'
    for p in step1_tf.paragraphs:
        for r in p.runs:
            r.font.size = Pt(15)
            r.font.color.rgb = WHITE
            p.alignment = PP_ALIGN.CENTER

    step2 = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(4.7), Inches(2.0), Inches(3.9), Inches(1.7))
    step2.fill.solid(); step2.fill.fore_color.rgb = ORANGE; step2.line.fill.background()
    step2_tf = step2.text_frame; step2_tf.text = 'Step 2\nFunds are held securely\nin the Booking Vault'
    for p in step2_tf.paragraphs:
        for r in p.runs:
            r.font.size = Pt(15)
            r.font.color.rgb = WHITE
            p.alignment = PP_ALIGN.CENTER

    step3 = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(9.2), Inches(2.0), Inches(3.4), Inches(1.7))
    step3.fill.solid(); step3.fill.fore_color.rgb = GREEN; step3.line.fill.background()
    step3_tf = step3.text_frame; step3_tf.text = 'Step 3\nProvider completes service\nJob is verified before release'
    for p in step3_tf.paragraphs:
        for r in p.runs:
            r.font.size = Pt(15)
            r.font.color.rgb = WHITE
            p.alignment = PP_ALIGN.CENTER

    note = slide.shapes.add_textbox(Inches(1.0), Inches(4.5), Inches(11.0), Inches(1.0))
    ntf = note.text_frame
    p = ntf.paragraphs[0]
    r = p.runs[0] if p.runs else p.add_run()
    r.text = 'The Booking Vault protects both parties by preventing immediate payment release until successful job verification.'
    r.font.size = Pt(19)
    r.font.color.rgb = TEXT

    add_footer(slide, 8)


def add_security_handshake_slide():
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_bg(slide, WHITE)
    add_title(slide, 'Security Handshake & Job Completion', '4-digit OTP verification and trust-building feedback')

    bullets = [
        'Upon booking acceptance, a unique 4-digit OTP is generated for the job.',
        'When the service provider completes the task, the client validates the OTP.',
        'A correct entry triggers fund release from the Booking Vault to the provider balance.',
        'Ratings and reviews are linked to completed bookings to strengthen accountability.'
    ]
    add_bullets(slide, bullets, font_size=19)
    add_footer(slide, 9)


def add_db_slide():
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_bg(slide, WHITE)
    add_title(slide, 'Database Design', 'Firebase Firestore collections and financial records')

    col1 = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.7), Inches(2.1), Inches(2.4), Inches(3.2))
    col1.fill.solid(); col1.fill.fore_color.rgb = BLUE; col1.line.fill.background()
    c1tf = col1.text_frame; c1tf.text = 'Users\nWorkers\nServices\nBookings'
    for p in c1tf.paragraphs:
        for r in p.runs:
            r.font.size = Pt(18)
            r.font.color.rgb = WHITE
            p.alignment = PP_ALIGN.CENTER

    col2 = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(3.6), Inches(2.1), Inches(2.5), Inches(3.2))
    col2.fill.solid(); col2.fill.fore_color.rgb = ORANGE; col2.line.fill.background()
    c2tf = col2.text_frame; c2tf.text = 'Payments\nBooking Vault\nCompletion Codes'
    for p in c2tf.paragraphs:
        for r in p.runs:
            r.font.size = Pt(18)
            r.font.color.rgb = WHITE
            p.alignment = PP_ALIGN.CENTER

    col3 = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(6.7), Inches(2.1), Inches(2.8), Inches(3.2))
    col3.fill.solid(); col3.fill.fore_color.rgb = GREEN; col3.line.fill.background()
    c3tf = col3.text_frame; c3tf.text = 'Ratings\nMessages\nService records\nAudit logs'
    for p in c3tf.paragraphs:
        for r in p.runs:
            r.font.size = Pt(18)
            r.font.color.rgb = WHITE
            p.alignment = PP_ALIGN.CENTER

    note = slide.shapes.add_textbox(Inches(9.9), Inches(2.7), Inches(2.7), Inches(2.2))
    ntf = note.text_frame
    ntf.text = 'Firestore stores application data as JSON-like documents for rapid real-time updates and scalable access.'
    for p in ntf.paragraphs:
        for r in p.runs:
            r.font.size = Pt(16)
            r.font.color.rgb = TEXT

    add_footer(slide, 10)


def add_ui_slide():
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_bg(slide, WHITE)
    add_title(slide, 'System Interfaces & UI Screens', 'Core user experiences across the platform')

    bullets = [
        'Client Dashboard: search, booking, and tracking experience for service requests.',
        'Service Provider Dashboard: active jobs, earnings summary, and job management.',
        'Admin Dashboard: monitoring, user administration, and platform oversight.',
        'Payment & Checkout: secure PayFast integration with a seamless transaction flow.'
    ]
    add_bullets(slide, bullets, font_size=19)
    add_footer(slide, 11)


def add_testing_slide():
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_bg(slide, WHITE)
    add_title(slide, 'Testing & Performance', 'Quality assurance and responsiveness')

    bullets = [
        'Testing strategy includes unit, UI, and device-based evaluation of app workflows.',
        'Performance target: smooth Jetpack Compose rendering with low-latency Firestore synchronization.',
        'Secure HTTPS communication adds transport-layer protection for client-to-backend traffic.',
        'The system is designed for trusted, scalable service operations across multiple users.'
    ]
    add_bullets(slide, bullets, font_size=19)
    add_footer(slide, 12)


def add_conclusion_slide():
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_bg(slide, WHITE)
    add_title(slide, 'Conclusion & Future Enhancements', 'Project outcomes and next steps')

    left = slide.shapes.add_textbox(Inches(0.8), Inches(2.0), Inches(5.6), Inches(3.6))
    ltf = left.text_frame
    for idx, line in enumerate([
        'HustleFix delivers a secure and feature-rich mobile service marketplace.',
        'The platform resolves trust and payment challenges through Booking Vault controls and OTP-based completion.',
        'The architecture demonstrates a practical, scalable digital marketplace model for Android users.'
    ]):
        p = ltf.paragraphs[0] if idx == 0 else ltf.add_paragraph()
        p.text = line
        for r in p.runs:
            r.font.size = Pt(20)
            r.font.color.rgb = TEXT
            p.bullet = True

    right = slide.shapes.add_textbox(Inches(7.0), Inches(2.2), Inches(5.4), Inches(2.6))
    rtf = right.text_frame
    for idx, line in enumerate([
        'Future improvements:',
        '• GPS tracking for provider dispatch',
        '• Multi-currency and additional payment gateways',
        '• AI-driven recommendations and smarter matching'
    ]):
        p = rtf.paragraphs[0] if idx == 0 else rtf.add_paragraph()
        p.text = line
        for r in p.runs:
            r.font.size = Pt(18 if idx == 0 else 17)
            r.font.color.rgb = TEXT
            r.font.bold = idx == 0

    add_footer(slide, 13)


def add_qna_slide():
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    set_bg(slide, WHITE)
    title_box = slide.shapes.add_textbox(Inches(0.8), Inches(1.8), Inches(8.0), Inches(1.0))
    tf = title_box.text_frame
    p = tf.paragraphs[0]
    run = p.runs[0] if p.runs else p.add_run()
    run.text = 'Q & A'
    run.font.size = Pt(34)
    run.font.bold = True
    run.font.color.rgb = NAVY

    thank = slide.shapes.add_textbox(Inches(0.8), Inches(3.1), Inches(8.8), Inches(1.0))
    tf2 = thank.text_frame
    p2 = tf2.paragraphs[0]
    run2 = p2.runs[0] if p2.runs else p2.add_run()
    run2.text = 'Thank you for your attention.'
    run2.font.size = Pt(24)
    run2.font.color.rgb = TEXT

    link = slide.shapes.add_textbox(Inches(0.8), Inches(4.5), Inches(9.5), Inches(0.8))
    ltf = link.text_frame
    p3 = ltf.paragraphs[0]
    r3 = p3.runs[0] if p3.runs else p3.add_run()
    r3.text = 'GitHub: https://github.com/teeboy57/hustlefix'
    r3.font.size = Pt(18)
    r3.font.color.rgb = BLUE

    add_footer(slide, 14)


# Cover + content slides
add_cover_slide()

standard_slides = [
    ('Introduction & Background', [
        'The modern gig economy depends on reliable, verified local service providers.',
        'Traditional booking methods are often limited by trust, fraud risk, and communication gaps.',
        'HustleFix centralizes the marketplace and provides a secure digital platform for Android users.'
    ]),
    ('Problem Statement & Objectives', [
        'Financial fraud and booking disputes remain major concerns for service transactions.',
        'Users struggle to find verified professionals quickly and transparently.',
        'The project focuses on secure payments, verification, and milestone-based completion tracking.'
    ]),
    ('Project Scope & Target Users', [
        'Scope includes Android app delivery, role-based access, and payment integration.',
        'Target users are Clients, Service Providers, and Administrators.',
        'The solution is designed for household repairs, tutoring, cleaning, and emergency services.'
    ]),
    ('Core Functional Modules', [
        'User & auth management with role-based access control.',
        'Profile and service management for service providers.',
        'Search, discovery, booking lifecycle tracking, and emergency request workflows.'
    ]),
    ('System Architecture', 'placeholder'),
    ('Software Architecture & Layering', 'placeholder'),
    ('Booking Vault & Payment Workflow', 'placeholder'),
    ('Security Handshake & Job Completion', 'placeholder'),
    ('Database Design', 'placeholder'),
    ('System Interfaces & UI Screens', 'placeholder'),
    ('Testing & Performance', 'placeholder'),
    ('Conclusion & Future Enhancements', 'placeholder'),
    ('Q & A', 'placeholder'),
]

# Slide 2-4, 7, 10-14
slide_content = [
    ('Introduction & Background', [
        'The modern gig economy depends on reliable, verified local service providers.',
        'Traditional booking methods are often limited by trust, fraud risk, and communication gaps.',
        'HustleFix centralizes the marketplace and provides a secure digital platform for Android users.'
    ], 2),
    ('Problem Statement & Objectives', [
        'Financial fraud and booking disputes remain major concerns for service transactions.',
        'Users struggle to find verified professionals quickly and transparently.',
        'The project focuses on secure payments, verification, and milestone-based completion tracking.'
    ], 3),
    ('Project Scope & Target Users', [
        'Scope includes Android app delivery, role-based access, and payment integration.',
        'Target users are Clients, Service Providers, and Administrators.',
        'The solution is designed for household repairs, tutoring, cleaning, and emergency services.'
    ], 4),
    ('Core Functional Modules', [
        'User & auth management with role-based access control.',
        'Profile and service management for service providers.',
        'Search, discovery, booking lifecycle tracking, and emergency request workflows.'
    ], 7),
    ('System Architecture', None, 5),
    ('Software Architecture & Layering', None, 6),
    ('Booking Vault & Payment Workflow', None, 8),
    ('Security Handshake & Job Completion', None, 9),
    ('Database Design', None, 10),
    ('System Interfaces & UI Screens', None, 11),
    ('Testing & Performance', None, 12),
    ('Conclusion & Future Enhancements', None, 13),
    ('Q & A', None, 14),
]

# Fill standard slides with specific content
for title, bullets, slide_no in slide_content:
    if title == 'System Architecture':
        add_architecture_slide()
    elif title == 'Software Architecture & Layering':
        add_layered_architecture_slide()
    elif title == 'Booking Vault & Payment Workflow':
        add_payment_workflow_slide()
    elif title == 'Security Handshake & Job Completion':
        add_security_handshake_slide()
    elif title == 'Database Design':
        add_db_slide()
    elif title == 'System Interfaces & UI Screens':
        add_ui_slide()
    elif title == 'Testing & Performance':
        add_testing_slide()
    elif title == 'Conclusion & Future Enhancements':
        add_conclusion_slide()
    elif title == 'Q & A':
        add_qna_slide()
    else:
        add_standard_slide(title, bullets, slide_no)

prs.save(OUT_PATH)
print(f'Created PowerPoint deck: {OUT_PATH}')
