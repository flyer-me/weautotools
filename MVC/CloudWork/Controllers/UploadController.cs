using Microsoft.AspNetCore.Mvc;

namespace BlogManagementApp.Controllers
{
    [Route("file/[controller]")]
    [ApiController]
    public class UploadController : ControllerBase
    {
        private readonly IWebHostEnvironment _environment;

        public UploadController(IWebHostEnvironment environment)
        {
            _environment = environment;
        }

        //POST file/upload
        [HttpPost]
        [IgnoreAntiforgeryToken]
        public async Task<IActionResult> FileUpload(IFormFile upload)
        {
            try
            {
                if (upload == null || upload.Length == 0)
                {
                    return BadRequest(new { error = new { message = "不能处理空文件" } });
                }

                var extension = Path.GetExtension(upload.FileName).ToLowerInvariant();
                /*
                var disallowedExtensions = new[] { ".sh", ".bat", ".exe", ".js" };
                if (string.IsNullOrEmpty(extension) || disallowedExtensions.Contains(extension))
                {
                    return BadRequest(new { error = new { message = "不支持的文件类型" } });
                }
                */

                var uniqueFileName = upload.FileName + Guid.NewGuid().ToString() + extension;
                var uploadsFolder = Path.Combine(_environment.WebRootPath, "uploads");

                if (!Directory.Exists(uploadsFolder))
                {
                    Directory.CreateDirectory(uploadsFolder);
                }

                var filePath = Path.Combine(uploadsFolder, uniqueFileName);

                using (var stream = new FileStream(filePath, FileMode.Create))
                {
                    await upload.CopyToAsync(stream);
                }

                var response = new
                {
                    uploaded = true,
                    url = Url.Content($"~/uploads/{uniqueFileName}")
                };

                return Ok(response);
                // return Ok(new { uploaded = true, url = Url.Content($"~/uploads/{uniqueFileName}") });
            }
            catch (IOException)
            {
                return StatusCode(500, new { error = new { message = "远程服务处理文件出错，请重试" } });
            }
            catch (Exception)
            {
                return StatusCode(500, new { error = new { message = "发生未知错误，请稍后" } });
            }
        }
    }
}