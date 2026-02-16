package com.cegb03.archeryscore.data

/*
val sampleProducts = listOf(
    Product(
        id = 1,
        name = "Mermelada de Frutilla",
        description = "Hecha con frutas orgánicas de la región.",
        price = "$800",
        imageUrl = "https://www.bing.com/images/search?view=detailV2&ccid=s2Pbt8aM&id=B56C28F1889C4C438DB3ABE1BEF062836BC5A033&thid=OIP.s2Pbt8aMZ89BNRupitAL3gHaEK&mediaurl=https%3a%2f%2fmedia.airedesantafe.com.ar%2fp%2f2fe4a2e909f0c089160ecdbad8748b33%2fadjuntos%2f268%2fimagenes%2f003%2f780%2f0003780168%2f1200x0%2fsmart%2fimagepng.png&cdnurl=https%3a%2f%2fth.bing.com%2fth%2fid%2fR.b363dbb7c68c67cf41351ba98ad00bde%3frik%3dM6DFa4Ni8L7hqw%26pid%3dImgRaw%26r%3d0&exph=675&expw=1200&q=mermelada+de+frutilla&simid=608035669405729296&FORM=IRPRST&ck=8CD3DE7F9B4AFA457D4D5B6552D9E2D6&selectedIndex=2&itb=0&ajaxhist=0&ajaxserp=0",
        category = "Alimentos"
    ),
    Product(
        id = 2,
        name = "Cartera tejida artesanal",
        description = "Cartera hecha a mano por artesanos locales.",
        price = "$4.500",
        imageUrl = "https://www.bing.com/images/search?view=detailV2&ccid=GMEVzri3&id=3E27AA6FFE168B17BAEB6DD3EDE37283FFA53B95&thid=OIP.GMEVzri3iCniTQcNlw14_QHaFj&mediaurl=https%3a%2f%2fi.pinimg.com%2foriginals%2f8c%2f0f%2f55%2f8c0f55a8cf3d0bc0f516ad99a68b2f8e.jpg&cdnurl=https%3a%2f%2fth.bing.com%2fth%2fid%2fR.18c115ceb8b78829e24d070d970d78fd%3frik%3dlTul%252f4Ny4%252b3TbQ%26pid%3dImgRaw%26r%3d0&exph=600&expw=800&q=Cartera+tejida+artesanal&simid=608039358772414289&FORM=IRPRST&ck=33091B448DDAC005EC69EC859AEBCEB3&selectedIndex=267&itb=0&qft=+filterui%3aphoto-photo&ajaxhist=0&ajaxserp=0",
        category = "Textiles"
    ),
    Product(
        id = 3,
        name = "Shampoo sólido natural",
        description = "Sin químicos ni parabenos. Ideal para cabello seco.",
        price = "$2.000",
        imageUrl = "https://www.bing.com/images/search?view=detailV2&ccid=EkVPabEH&id=42622D6EBAD9B40035CA45B90C3410F434266372&thid=OIP.EkVPabEHtp0TCZCsbsitEAHaE8&mediaurl=https%3a%2f%2fwww.nexofin.com%2farchivos%2f2021%2f04%2fshampoo-solido-come-si-usa-scaled-e1613394303744.jpg&cdnurl=https%3a%2f%2fth.bing.com%2fth%2fid%2fR.12454f69b107b69d130990ac6ec8ad10%3frik%3dcmMmNPQQNAy5RQ%26pid%3dImgRaw%26r%3d0&exph=854&expw=1280&q=Shampoo+s%c3%b3lido+natural&simid=607996112742525782&FORM=IRPRST&ck=E593BC8E6C54286BB868F3FA7A71BAF7&selectedIndex=4&itb=0&ajaxhist=0&ajaxserp=0",
        category = "Cosmética Natural"
    ),
    Product(
        id = 4,
        name = "Pan casero integral",
        description = "Pan recién horneado con harina orgánica.",
        price = "$700",
        imageUrl = "x",
        category = "Alimentos"
    ),
    Product(
        id = 5,
        name = "Aro de cerámica artesanal",
        description = "Aros pintados a mano, únicos e irrepetibles.",
        price = "$1.200",
        imageUrl = "x",
        category = "Artesanías"
    ),
    Product(
        id = 6,
        name = "Jabón de lavanda",
        description = "Jabón artesanal con aceite esencial de lavanda.",
        price = "$600",
        imageUrl = "x",
        category = "Cosmética Natural"
    ),
    Product(
        id = 7,
        name = "Bolso de lona reciclada",
        description = "Confeccionado con telas reutilizadas, resistente y ecológico.",
        price = "$3.200",
        imageUrl = "x",
        category = "Textiles"
    ),
    Product(
        id = 8,
        name = "Miel pura",
        description = "Miel 100% natural, extraída en campos locales.",
        price = "$1.800",
        imageUrl = "x",
        category = "Alimentos"
    ),
    Product(
        id = 9,
        name = "Collar de macramé",
        description = "Diseño boho-chic hecho a mano con hilo de algodón.",
        price = "$2.000",
        imageUrl = "x",
        category = "Artesanías"
    ),
    Product(
        id = 10,
        name = "Crema facial natural",
        description = "Hidratante con aloe vera y karité, sin fragancias sintéticas.",
        price = "$2.500",
        imageUrl = "x",
        category = "Cosmética Natural"
    ),
    Product(
        id = 11,
        name = "Camiseta orgánica estampada",
        description = "Diseños exclusivos sobre algodón orgánico.",
        price = "$4.000",
        imageUrl = "x",
        category = "Textiles"
    ),
    Product(
        id = 12,
        name = "Alfajor de maicena artesanal",
        description = "Relleno con dulce de leche casero y coco rallado.",
        price = "$500",
        imageUrl = "x",
        category = "Alimentos"
    ),
    Product(
        id = 13,
        name = "Cuadro en madera reciclada",
        description = "Obra decorativa hecha con materiales reutilizados.",
        price = "$6.000",
        imageUrl = "x",
        category = "Artesanías"
    ),
    Product(
        id = 14,
        name = "Desodorante natural en barra",
        description = "Efectivo, libre de aluminio y amigable con la piel.",
        price = "$1.200",
        imageUrl = "x",
        category = "Cosmética Natural"
    ),
    Product(
        id = 15,
        name = "Mate artesanal calabaza",
        description = "Forrado en cuero, pintado a mano por emprendedores.",
        price = "$3.000",
        imageUrl = "x",
        category = "Artesanías"
    ),
    Product(
        id = 16,
        name = "Té de hierbas serranas",
        description = "Blend natural de hierbas cultivadas en montaña.",
        price = "$900",
        imageUrl = "x",
        category = "Alimentos"
    ),
    Product(
        id = 17,
        name = "Riñonera con tela estampada",
        description = "Práctica y colorida, perfecta para ferias o salidas.",
        price = "$2.800",
        imageUrl = "x",
        category = "Textiles"
    ),
    Product(
        id = 18,
        name = "Acondicionador sólido natural",
        description = "Repara e hidrata el cabello, sin siliconas.",
        price = "$1.800",
        imageUrl = "x",
        category = "Cosmética Natural"
    ),
    Product(
        id = 19,
        name = "Velas aromáticas de soja",
        description = "Velas ecológicas con fragancias naturales.",
        price = "$1.300",
        imageUrl = "x",
        category = "Artesanías"
    ),
    Product(
        id = 20,
        name = "Yerba mate agroecológica",
        description = "Secado natural sin humo, sabor suave.",
        price = "$2.200",
        imageUrl = "x",
        category = "Alimentos"
    ),
    Product(
        id = 21,
        name = "Tarta de zapallo casera",
        description = "Tarta saludable con masa integral y vegetales frescos.",
        price = "$1.200",
        imageUrl = "x",
        category = "Alimentos"
    ),
    Product(
        id = 22,
        name = "Bufanda tejida a mano",
        description = "Lana pura de oveja, ideal para el invierno.",
        price = "$3.000",
        imageUrl = "x",
        category = "Textiles"
    ),
    Product(
        id = 23,
        name = "Aceite corporal de almendras",
        description = "Nutre la piel profundamente con aroma suave.",
        price = "$2.300",
        imageUrl = "x",
        category = "Cosmética Natural"
    ),
    Product(
        id = 24,
        name = "Cuenco de barro cocido",
        description = "Hecho a mano, ideal para servir salsas o snacks.",
        price = "$950",
        imageUrl = "x",
        category = "Artesanías"
    ),
    Product(
        id = 25,
        name = "Chips de batata deshidratada",
        description = "Snack saludable y crocante, sin conservantes.",
        price = "$600",
        imageUrl = "x",
        category = "Alimentos"
    ),
    Product(
        id = 26,
        name = "Pantalón de lino artesanal",
        description = "Cómodo, fresco y teñido naturalmente.",
        price = "$4.800",
        imageUrl = "x",
        category = "Textiles"
    ),
    Product(
        id = 27,
        name = "Bálsamo labial natural",
        description = "Con manteca de karité y aceites esenciales.",
        price = "$850",
        imageUrl = "x",
        category = "Cosmética Natural"
    ),
    Product(
        id = 28,
        name = "Posavasos de madera reciclada",
        description = "Set de 4 piezas con diseños grabados a mano.",
        price = "$1.100",
        imageUrl = "x",
        category = "Artesanías"
    ),
    Product(
        id = 29,
        name = "Galletas de avena y miel",
        description = "Sin azúcar refinada, ideales para meriendas.",
        price = "$700",
        imageUrl = "x",
        category = "Alimentos"
    ),
    Product(
        id = 30,
        name = "Top de algodón orgánico",
        description = "Tejido a mano con técnicas sostenibles.",
        price = "$3.500",
        imageUrl = "x",
        category = "Textiles"
    ),
    Product(
        id = 31,
        name = "Crema corporal de caléndula",
        description = "Alivio para piel sensible y enrojecida.",
        price = "$1.900",
        imageUrl = "x",
        category = "Cosmética Natural"
    ),
    Product(
        id = 32,
        name = "Llavero de cuero trabajado",
        description = "Artesanía con iniciales personalizadas.",
        price = "$600",
        imageUrl = "x",
        category = "Artesanías"
    ),
    Product(
        id = 33,
        name = "Granola artesanal con frutos secos",
        description = "Perfecta para desayuno o snack saludable.",
        price = "$950",
        imageUrl = "x",
        category = "Alimentos"
    ),
    Product(
        id = 34,
        name = "Falda bordada a mano",
        description = "Con detalles florales tradicionales.",
        price = "$4.200",
        imageUrl = "x",
        category = "Textiles"
    ),
    Product(
        id = 35,
        name = "Mascarilla capilar nutritiva",
        description = "Fortalece el cabello con ingredientes naturales.",
        price = "$2.400",
        imageUrl = "x",
        category = "Cosmética Natural"
    ),
    Product(
        id = 36,
        name = "Florero cerámico esmaltado",
        description = "Color tierra, diseño rústico y elegante.",
        price = "$2.000",
        imageUrl = "x",
        category = "Artesanías"
    ),
    Product(
        id = 37,
        name = "Pickles de pepino caseros",
        description = "Conservados en vinagre de manzana orgánico.",
        price = "$1.100",
        imageUrl = "x",
        category = "Alimentos"
    ),
    Product(
        id = 38,
        name = "Camisa de algodón natural",
        description = "Diseño unisex con botones de coco.",
        price = "$5.000",
        imageUrl = "x",
        category = "Textiles"
    ),
    Product(
        id = 39,
        name = "Perfume sólido artesanal",
        description = "Fragancia suave con cera de abeja y aceites esenciales.",
        price = "$1.500",
        imageUrl = "x",
        category = "Cosmética Natural"
    ),
    Product(
        id = 40,
        name = "Decoración en macramé",
        description = "Ideal para colgar en paredes o ventanas.",
        price = "$2.600",
        imageUrl = "x",
        category = "Artesanías"
    )
)*/
