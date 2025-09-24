package com.cocido.nonna.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.cocido.nonna.R
import com.cocido.nonna.domain.model.Person
import com.cocido.nonna.domain.model.Relation
import kotlin.math.*

/**
 * Custom View para dibujar el árbol genealógico
 * Soporta zoom, pan y gestos táctiles
 */
class GenealogyCanvas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var scaleFactor = 1.0f
    private var focusX = 0f
    private var focusY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    
    private var persons = listOf<Person>()
    private var relations = listOf<Relation>()
    
    private val gestureDetector: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector
    
    private var onPersonClickListener: ((Person) -> Unit)? = null
    
    init {
        setupPaints()
        
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true
            
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                focusX -= distanceX
                focusY -= distanceY
                invalidate()
                return true
            }
            
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val person = getPersonAt(e.x, e.y)
                person?.let { onPersonClickListener?.invoke(it) }
                return true
            }
        })
        
        scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                scaleFactor = scaleFactor.coerceIn(0.5f, 3.0f)
                invalidate()
                return true
            }
        })
    }
    
    private fun setupPaints() {
        // Paint para círculos de personas
        paint.color = context.getColor(R.color.primary_warm)
        paint.style = Paint.Style.FILL
        
        // Paint para texto
        textPaint.color = context.getColor(R.color.text_primary)
        textPaint.textSize = 32f
        textPaint.textAlign = Paint.Align.CENTER
        
        // Paint para líneas de relaciones
        linePaint.color = context.getColor(R.color.primary_warm)
        linePaint.strokeWidth = 4f
        linePaint.style = Paint.Style.STROKE
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        canvas.save()
        canvas.scale(scaleFactor, scaleFactor, focusX, focusY)
        
        // Dibujar líneas de relaciones
        drawRelations(canvas)
        
        // Dibujar personas
        drawPersons(canvas)
        
        canvas.restore()
    }
    
    private fun drawRelations(canvas: Canvas) {
        relations.forEach { relation ->
            val fromPerson = persons.find { it.id == relation.from }
            val toPerson = persons.find { it.id == relation.to }
            
            if (fromPerson != null && toPerson != null) {
                val fromPos = getPersonPosition(fromPerson)
                val toPos = getPersonPosition(toPerson)
                
                canvas.drawLine(
                    fromPos.x, fromPos.y,
                    toPos.x, toPos.y,
                    linePaint
                )
            }
        }
    }
    
    private fun drawPersons(canvas: Canvas) {
        persons.forEach { person ->
            val position = getPersonPosition(person)
            
            // Dibujar círculo de la persona
            canvas.drawCircle(position.x, position.y, 40f, paint)
            
            // Dibujar nombre
            val name = person.fullName.split(" ").first() // Solo primer nombre
            canvas.drawText(
                name,
                position.x,
                position.y + 10f,
                textPaint
            )
        }
    }
    
    private fun getPersonPosition(person: Person): PointF {
        // Algoritmo básico de posicionamiento jerárquico
        val relations = this.relations.filter { 
            it.from == person.id || it.to == person.id 
        }
        
        // Si es una persona sin relaciones, posicionar en el centro
        if (relations.isEmpty()) {
            return PointF(width / 2f, height / 2f)
        }
        
        // Encontrar la generación de la persona
        val generation = calculateGeneration(person.id)
        
        // Calcular posición basada en la generación y orden dentro de la generación
        val sameGenerationPersons = persons.filter { 
            calculateGeneration(it.id) == generation 
        }.sortedBy { it.fullName }
        
        val indexInGeneration = sameGenerationPersons.indexOf(person)
        val totalInGeneration = sameGenerationPersons.size
        
        // Posicionar horizontalmente
        val x = if (totalInGeneration == 1) {
            width / 2f
        } else {
            val spacing = width / (totalInGeneration + 1f)
            spacing * (indexInGeneration + 1)
        }
        
        // Posicionar verticalmente basado en la generación
        val y = 100f + generation * 150f
        
        return PointF(x, y)
    }
    
    private fun calculateGeneration(personId: com.cocido.nonna.domain.model.PersonId): Int {
        // Encontrar la generación de una persona basándose en las relaciones parent-child
        val visited = mutableSetOf<com.cocido.nonna.domain.model.PersonId>()
        val queue = mutableListOf<Pair<com.cocido.nonna.domain.model.PersonId, Int>>()
        
        // Empezar con la persona actual
        queue.add(Pair(personId, 0))
        
        while (queue.isNotEmpty()) {
            val (currentId, generation) = queue.removeAt(0)
            
            if (currentId in visited) continue
            visited.add(currentId)
            
            // Si llegamos a la persona que buscamos, devolver su generación
            if (currentId == personId && generation > 0) {
                return generation
            }
            
            // Buscar padres (relaciones donde la persona actual es el hijo)
            val parentRelations = relations.filter { 
                it.to == currentId && it.type == com.cocido.nonna.domain.model.RelationType.PARENT 
            }
            
            for (relation in parentRelations) {
                queue.add(Pair(relation.from, generation + 1))
            }
            
            // Si no hay padres, esta es la generación más alta (0)
            if (parentRelations.isEmpty() && currentId == personId) {
                return 0
            }
        }
        
        return 0 // Default
    }
    
    private fun getPersonAt(x: Float, y: Float): Person? {
        val adjustedX = (x - focusX) / scaleFactor
        val adjustedY = (y - focusY) / scaleFactor
        
        persons.forEach { person ->
            val position = getPersonPosition(person)
            val distance = sqrt((adjustedX - position.x).pow(2) + (adjustedY - position.y).pow(2))
            if (distance <= 40f) {
                return person
            }
        }
        return null
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        if (!scaleGestureDetector.isInProgress) {
            gestureDetector.onTouchEvent(event)
        }
        return true
    }
    
    fun setData(persons: List<Person>, relations: List<Relation>) {
        this.persons = persons
        this.relations = relations
        invalidate()
    }
    
    fun setOnPersonClickListener(listener: (Person) -> Unit) {
        onPersonClickListener = listener
    }
    
    fun zoomIn() {
        scaleFactor = (scaleFactor * 1.2f).coerceAtMost(3.0f)
        invalidate()
    }
    
    fun zoomOut() {
        scaleFactor = (scaleFactor / 1.2f).coerceAtLeast(0.5f)
        invalidate()
    }
    
    fun resetZoom() {
        scaleFactor = 1.0f
        focusX = width / 2f
        focusY = height / 2f
        invalidate()
    }
}


