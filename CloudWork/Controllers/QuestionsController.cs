using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using CloudWork.Data;
using CloudWork.Models;
using CloudWork.Repository;

namespace CloudWork.Controllers
{
    public class QuestionsController : Controller
    {
        private readonly CloudWorkDbContext _context;
        private readonly IGenericRepository<Question> _genericRepository;

        public QuestionsController(IGenericRepository<Question> genericRepository, CloudWorkDbContext context)
        {
            _genericRepository = genericRepository;
            _context = context;
        }

        // GET: Questions
        public async Task<IActionResult> Index()
        {
            var questions = await _genericRepository.GetAllAsync();
            return View(questions);
        }

        // GET: Questions/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var question = await _genericRepository.GetByIdAsync(id.Value);
            if (question == null)
            {
                return NotFound();
            }

            return View(question);
        }

        // GET: Questions/Create
        public IActionResult Create()
        {
            return View();
        }

        // POST: Questions/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("Id,Title,Description,IsPublic")] Question question)
        {
            if (ModelState.IsValid)
            {
                await _genericRepository.AddAsync(question);
                await _genericRepository.SaveAsync();
                return RedirectToAction(nameof(Index));
            }
            return View(question);
        }

        // GET: Questions/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var question = await _genericRepository.GetByIdAsync(id.Value);
            if (question == null)
            {
                return NotFound();
            }
            return View(question);
        }

        // POST: Questions/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("Id,Title,Description,IsPublic")] Question question)
        {
            if (id != question.Id)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _genericRepository.Update(question);
                    await _genericRepository.SaveAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    var que = await _genericRepository.GetByIdAsync(question.Id);
                    if (que == null)
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }
            return View(question);
        }

        // GET: Questions/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var question = await _genericRepository.GetByIdAsync(id.Value);
            if (question == null)
            {
                return NotFound();
            }

            return View(question);
        }

        // POST: Questions/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var question = await _genericRepository.GetByIdAsync(id);
            if (question != null)
            {
                await _genericRepository.DeleteAsync(id);
                await _genericRepository.SaveAsync();
            }

            return RedirectToAction(nameof(Index));
        }

    }
}
